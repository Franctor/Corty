package com.corty.backend.services;

import com.corty.backend.dto.BookingCreateRequest;
import com.corty.backend.dto.BookingCreateResponse;
import com.corty.backend.dto.BookingDetailResponse;
import com.corty.backend.dto.BookingListItemResponse;
import com.corty.backend.dto.CancellationResponse;
import com.corty.backend.dto.NextBookingResponse;
import com.corty.backend.dto.RecentActivityResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import com.corty.backend.mapper.BookingMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Court;
import com.corty.backend.model.Player;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.PlayerSport;
import com.corty.backend.model.User;
import com.corty.backend.model.ClubBalanceEntry;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.model.enums.ClubBalanceReason;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.model.enums.Team;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.ClubBalanceEntryRepository;
import com.corty.backend.repository.JoinRequestRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.PlayerSportRepository;
import com.corty.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final CourtRepository courtRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final JoinRequestRepository joinRequestRepository;
    private final PlayerSportRepository playerSportRepository;
    private final StripeService stripeService;
    private final ClubBalanceEntryRepository clubBalanceEntryRepository;

    public Optional<NextBookingResponse> getNextBooking(String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);

        List<Booking> upcoming = bookingRepository.findUpcomingByUserOrPlayer(
                user.getIdUser(), player.getIdPlayer(), LocalDate.now()
        );

        return upcoming.stream().findFirst()
                .map(bookingMapper::toNextBookingResponse);
    }

    public BookingDetailResponse getBookingDetail(Long bookingId, String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);
        Booking booking = resolveBooking(bookingId);

        // Reservas públicas: cualquier jugador autenticado puede ver el detalle
        boolean isPublic = booking.getBookingType() == BookingType.PUBLIC;
        boolean isOwner = booking.getOwner().getIdUser().equals(user.getIdUser());
        boolean isParticipant = booking.getParticipants().stream()
                .anyMatch(pb -> pb.getPlayer().getIdPlayer().equals(player.getIdPlayer()));

        if (!isPublic && !isOwner && !isParticipant) {
            throw new AccessDeniedException("No tienes acceso a esta reserva");
        }

        BookingDetailResponse response = bookingMapper.toBookingDetailResponse(booking);

        List<BookingDetailResponse.ParticipantResponse> participants = booking.getParticipants().stream()
                .map(pb -> {
                    BookingDetailResponse.ParticipantResponse p = bookingMapper.toParticipantResponse(pb);
                    p.setOwner(booking.getOwner().getIdUser().equals(pb.getPlayer().getUser().getIdUser()));
                    p.setCurrentUser(pb.getPlayer().getIdPlayer().equals(player.getIdPlayer()));
                    return p;
                })
                .collect(Collectors.toList());

        response.setParticipants(participants);
        response.setCurrentUserOwner(isOwner);
        response.setCurrentUserParticipant(isParticipant);

        if (isPublic && !isOwner && !isParticipant) {
            joinRequestRepository.findByPlayerAndBooking(player.getIdPlayer(), bookingId)
                    .ifPresent(jr -> response.setMyJoinRequestStatus(jr.getStatus().name()));
        }

        return response;
    }

    /** Owner cancela la reserva completa */
    @Transactional
    public CancellationResponse cancelBooking(Long bookingId, String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);
        Booking booking = resolveBooking(bookingId);

        if (!booking.getOwner().getIdUser().equals(user.getIdUser())) {
            throw new AccessDeniedException("Solo el propietario puede cancelar la reserva");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessLogicException("La reserva ya está cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new BusinessLogicException("No se puede cancelar una reserva completada");
        }

        // Reserva con pago pendiente: cancelar sin penalización ni notificaciones (nadie pagó nada)
        if (booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            return CancellationResponse.builder()
                    .message("La reserva ha sido cancelada.")
                    .karmaDeducted(0)
                    .karmaRemaining(player.getKarma())
                    .refundInfo("No se había realizado ningún pago.")
                    .build();
        }

        CancellationPolicy policy = new CancellationPolicy(booking);
        int penalty = policy.getKarmaPenalty();

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        applyKarmaPenalty(player, penalty);

        String clubName = booking.getCourt().getClub().getName();
        Long bookingId2 = booking.getIdBooking();

        // Reembolso al owner según ventana (sobre su PaymentIntent)
        if (booking.getPaymentId() != null && booking.getPaymentMethod() == PaymentMethod.ONLINE) {
            BigDecimal ownerPaid = booking.getTotalPrice();
            BigDecimal ownerRefundFraction = switch (policy.getWindow()) {
                case FREE      -> BigDecimal.ONE;
                case PARTIAL   -> new BigDecimal("0.5");
                case NO_REFUND -> BigDecimal.ZERO;
            };
            BigDecimal ownerRefund = ownerPaid.multiply(ownerRefundFraction).setScale(2, RoundingMode.HALF_UP);
            BigDecimal clubGains   = ownerPaid.subtract(ownerRefund);

            if (ownerRefund.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    long refundCents = ownerRefund.multiply(BigDecimal.valueOf(100))
                            .setScale(0, RoundingMode.HALF_UP).longValue();
                    Refund.create(RefundCreateParams.builder()
                            .setPaymentIntent(booking.getPaymentId())
                            .setAmount(refundCents)
                            .build());
                    log.info("Reembolso {}€ al owner de reserva {}", ownerRefund, bookingId2);
                } catch (StripeException e) {
                    log.error("Error al reembolsar al owner de reserva {}: {}", bookingId2, e.getMessage());
                }
            }

            // Registrar ganancia del club si aplica
            if (clubGains.compareTo(BigDecimal.ZERO) > 0) {
                ClubBalanceReason reason = policy.getWindow() == CancellationPolicy.Window.PARTIAL
                        ? ClubBalanceReason.OWNER_LATE_CANCEL
                        : ClubBalanceReason.OWNER_LAST_MINUTE_CANCEL;
                recordClubBalance(booking, clubGains, reason,
                        "Owner canceló reserva (" + policy.getWindow() + ") en " + clubName);
            }
        }

        // Reembolsar 100% a todos los participantes que pagaron (no tienen culpa)
        booking.getParticipants().forEach(pb -> {
            issueRefund(pb, BigDecimal.ONE, "Reserva cancelada por el organizador en " + clubName);
            notificationService.send(
                    pb.getPlayer().getUser().getIdUser(),
                    NotificationType.BOOKING_CANCELLED,
                    "Reserva cancelada",
                    "La reserva en " + clubName + " ha sido cancelada por el organizador",
                    bookingId2
            );
        });

        return CancellationResponse.builder()
                .message(policy.getCancellationMessage(true))
                .karmaDeducted(penalty)
                .karmaRemaining(player.getKarma())
                .refundInfo(policy.getRefundInfo(true))
                .build();
    }

    /** Participante abandona la reserva */
    @Transactional
    public CancellationResponse leaveBooking(Long bookingId, String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);
        Booking booking = resolveBooking(bookingId);

        if (booking.getOwner().getIdUser().equals(user.getIdUser())) {
            throw new BusinessLogicException("El propietario no puede abandonar la reserva; usa cancelar reserva");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessLogicException("La reserva ya está cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new BusinessLogicException("No se puede abandonar una reserva completada");
        }

        PlayerBooking pb = playerBookingRepository
                .findByBookingIdAndPlayerId(bookingId, player.getIdPlayer())
                .orElseThrow(() -> new BusinessLogicException("No eres participante de esta reserva"));

        CancellationPolicy policy = new CancellationPolicy(booking);
        int penalty = policy.getKarmaPenalty();

        String leaverName = player.getName() + " " + player.getSurname();
        String clubName = booking.getCourt().getClub().getName();
        Long bookingId2 = booking.getIdBooking();
        Long ownerUserId = booking.getOwner().getIdUser();

        // Recoger IDs de participantes a notificar antes de eliminar
        List<Long> participantUserIds = booking.getParticipants().stream()
                .filter(p -> !p.getPlayer().getIdPlayer().equals(player.getIdPlayer()))
                .filter(p -> !p.getPlayer().getUser().getIdUser().equals(ownerUserId))
                .map(p -> p.getPlayer().getUser().getIdUser())
                .toList();

        // Reembolso según ventana de cancelación (solo si splitPayment=true y pagó)
        BigDecimal refundFraction = switch (policy.getWindow()) {
            case FREE      -> BigDecimal.ONE;
            case PARTIAL   -> new BigDecimal("0.5");
            case NO_REFUND -> BigDecimal.ZERO;
        };
        if (booking.isSplitPayment() && pb.getPaidAmount() != null) {
            issueRefund(pb, refundFraction, "Abandono de reserva en " + clubName);

            // Registrar ganancia del club por la parte no reembolsada
            BigDecimal lostAmount = pb.getPaidAmount()
                    .multiply(BigDecimal.ONE.subtract(refundFraction))
                    .setScale(2, RoundingMode.HALF_UP);
            if (lostAmount.compareTo(BigDecimal.ZERO) > 0) {
                ClubBalanceReason reason = policy.getWindow() == CancellationPolicy.Window.PARTIAL
                        ? ClubBalanceReason.PARTICIPANT_LATE_CANCEL
                        : ClubBalanceReason.PARTICIPANT_LAST_MINUTE_CANCEL;
                recordClubBalance(booking, lostAmount, reason,
                        leaverName + " abandonó reserva (" + policy.getWindow() + ") en " + clubName);
            }
        }

        playerBookingRepository.deleteByBookingIdAndPlayerId(bookingId, player.getIdPlayer());
        joinRequestRepository.findByPlayerAndBooking(player.getIdPlayer(), bookingId)
                .ifPresent(joinRequestRepository::delete);
        applyKarmaPenalty(player, penalty);

        // Notificar después del delete
        notificationService.send(ownerUserId, NotificationType.PARTICIPANT_LEFT,
                "Jugador ha abandonado",
                leaverName + " ha abandonado la reserva en " + clubName, bookingId2);

        participantUserIds.forEach(uid -> notificationService.send(uid, NotificationType.PARTICIPANT_LEFT,
                "Jugador ha abandonado",
                leaverName + " ha abandonado la reserva en " + clubName, bookingId2));

        return CancellationResponse.builder()
                .message(policy.getCancellationMessage(false))
                .karmaDeducted(penalty)
                .karmaRemaining(player.getKarma())
                .refundInfo(policy.getRefundInfo(false))
                .build();
    }

    // ── Creación ─────────────────────────────────────────────────────────────

    @Transactional
    public BookingCreateResponse createBooking(BookingCreateRequest request, String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Pista no encontrada"));

        if (request.getBookingType() == BookingType.PUBLIC && player.getKarma() < 15) {
            throw new BusinessLogicException("Tu karma es demasiado bajo para crear partidos públicos. Juega partidos privados para recuperarlo.");
        }
        if (request.getDate().isEqual(LocalDate.now()) && !request.getStartTime().isAfter(java.time.LocalTime.now())) {
            throw new BusinessLogicException("No puedes reservar en un horario que ya ha pasado");
        }

        if (bookingRepository.existsOverlappingBooking(
                court.getIdCourt(), request.getDate(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessLogicException("El horario seleccionado ya está reservado");
        }

        long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        BigDecimal totalPrice = court.getPricePerHour()
                .multiply(BigDecimal.valueOf(durationMinutes))
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);

        BookingStatus initialStatus = request.getPaymentMethod() == PaymentMethod.CASH
                ? BookingStatus.CONFIRMED
                : BookingStatus.PENDING_PAYMENT;

        Booking booking = Booking.builder()
                .court(court)
                .owner(user)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookingType(request.getBookingType())
                .bookingStatus(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .splitPayment(request.isSplitPayment())
                .notes(request.getNotes())
                .totalPrice(totalPrice)
                .build();
        booking = bookingRepository.save(booking);

        ensurePlayerSportExists(player, court.getSport());
        PlayerBooking pb = PlayerBooking.builder()
                .booking(booking)
                .player(player)
                .team(Team.NONE)
                .splitPrice(totalPrice)
                .build();
        playerBookingRepository.save(pb);

        return new BookingCreateResponse(booking.getIdBooking());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Emite un reembolso parcial o total sobre el paidAmount de un PlayerBooking.
     * fraction=1 → 100%, fraction=0.5 → 50%, fraction=0 → sin reembolso.
     */
    private void issueRefund(PlayerBooking pb, BigDecimal fraction, String reason) {
        if (fraction.compareTo(BigDecimal.ZERO) == 0) return;
        BigDecimal paid = pb.getPaidAmount();
        if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) return;
        if (pb.getPaymentId() == null) return;

        BigDecimal refundAmount = paid.multiply(fraction).setScale(2, RoundingMode.HALF_UP);
        if (refundAmount.compareTo(new BigDecimal("0.50")) < 0) return; // mínimo Stripe

        try {
            long refundCents = refundAmount.multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP).longValue();
            Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(pb.getPaymentId())
                    .setAmount(refundCents)
                    .build());
            log.info("Reembolso de {}€ emitido al jugador {} ({})",
                    refundAmount, pb.getPlayer().getIdPlayer(), reason);
        } catch (StripeException e) {
            log.error("Error al reembolsar al jugador {}: {}", pb.getPlayer().getIdPlayer(), e.getMessage());
        }
    }

    private void recordClubBalance(Booking booking, BigDecimal amount, ClubBalanceReason reason, String description) {
        clubBalanceEntryRepository.save(ClubBalanceEntry.builder()
                .club(booking.getCourt().getClub())
                .booking(booking)
                .amount(amount)
                .reason(reason)
                .description(description)
                .build());
    }

    private void applyKarmaPenalty(Player player, int penalty) {
        if (penalty > 0) {
            int newKarma = Math.max(0, player.getKarma() - penalty);
            player.setKarma(newKarma);
            playerRepository.save(player);
        }
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Player resolvePlayer(User user) {
        return playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de jugador no encontrado"));
    }

    private void ensurePlayerSportExists(Player player, com.corty.backend.model.Sport sport) {
        boolean exists = playerSportRepository
                .findByPlayer_IdPlayerAndSport_IdSport(player.getIdPlayer(), sport.getIdSport())
                .isPresent();
        if (!exists) {
            PlayerSport ps = PlayerSport.builder()
                    .player(player)
                    .sport(sport)
                    .level(5.0)
                    .playedMatches(0)
                    .wins(0)
                    .losses(0)
                    .build();
            playerSportRepository.save(ps);
        }
    }

    private Booking resolveBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<BookingListItemResponse> getUserBookings(String username, List<BookingStatus> statuses) {
        User user = resolveUser(username);
        return bookingRepository.findByUserAndStatuses(user.getIdUser(), statuses).stream()
                .map(b -> BookingListItemResponse.builder()
                        .id(b.getIdBooking())
                        .courtName(b.getCourt().getName())
                        .clubName(b.getCourt().getClub().getName())
                        .clubLogoUrl(b.getCourt().getClub().getLogoUrl())
                        .sport(b.getCourt().getSport().getName())
                        .sportIconUrl(b.getCourt().getSport().getIconUrl())
                        .date(b.getDate())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .bookingStatus(b.getBookingStatus())
                        .totalPrice(b.getTotalPrice().doubleValue())
                        .fullyPaid(b.isFullyPaid())
                        .participantCount(b.getParticipants().size())
                        .build())
                .toList();
    }

    public List<RecentActivityResponse> getRecentActivity(String username) {
        User user = resolveUser(username);
        Player player = resolvePlayer(user);

        List<Booking> recent = bookingRepository.findRecentCompletedByPlayer(
                player.getIdPlayer(), PageRequest.of(0, 5)
        );

        return bookingMapper.toRecentActivityList(recent);
    }
}
