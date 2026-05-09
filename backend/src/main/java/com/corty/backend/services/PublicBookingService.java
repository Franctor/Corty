package com.corty.backend.services;

import com.corty.backend.dto.JoinRequestResponse;
import com.corty.backend.dto.PublicBookingResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.*;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.model.enums.JoinRequestStatus;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.model.enums.Team;
import com.corty.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicBookingService {

    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PlayerSportRepository playerSportRepository;
    private final StripeService stripeService;

    @Transactional(readOnly = true)
    public List<PublicBookingResponse> findPublicBookings(
            Double lat, Double lon, double radiusKm,
            String sportName, String dateFrom, String dateTo,
            Double levelMin, Double levelMax, int limit,
            String username) {

        Player currentPlayer = resolvePlayer(username);
        Long userId = currentPlayer.getUser().getIdUser();
        Long playerId = currentPlayer.getIdPlayer();
        List<Object[]> rows = bookingRepository.findPublicBookingsRaw(lat, lon, radiusKm, sportName, dateFrom, dateTo, levelMin, levelMax, limit, userId, playerId);

        List<PublicBookingResponse> results = rows.stream().map((Object[] row) -> {
            Long bookingId = ((Number) row[0]).longValue();
            Double distanceKm = row[1] != null ? ((Number) row[1]).doubleValue() : null;
            double avgLevel = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            Booking b = bookingRepository.findById(bookingId).orElseThrow();
            int currentPlayers = b.getParticipants().size();
            int maxPlayers = b.getCourt().getSport().getPlayersPerMatch();
            double pricePerPlayer = currentPlayers > 0
                    ? b.getTotalPrice().divide(BigDecimal.valueOf(currentPlayers), 2, RoundingMode.HALF_UP).doubleValue()
                    : b.getTotalPrice().doubleValue();

            Optional<JoinRequest> myRequest = joinRequestRepository
                    .findByPlayerAndBooking(currentPlayer.getIdPlayer(), bookingId);

            return PublicBookingResponse.builder()
                    .id(b.getIdBooking())
                    .courtName(b.getCourt().getName())
                    .clubName(b.getCourt().getClub().getName())
                    .clubLogoUrl(b.getCourt().getClub().getLogoUrl())
                    .clubLat(b.getCourt().getClub().getGeoLat())
                    .clubLng(b.getCourt().getClub().getGeoLong())
                    .sport(b.getCourt().getSport().getName())
                    .sportIconUrl(b.getCourt().getSport().getIconUrl())
                    .sportColor(b.getCourt().getSport().getColor())
                    .date(b.getDate())
                    .startTime(b.getStartTime())
                    .endTime(b.getEndTime())
                    .currentPlayers(currentPlayers)
                    .maxPlayers(maxPlayers)
                    .avgLevel(Math.round(avgLevel * 10.0) / 10.0)
                    .totalPrice(b.getTotalPrice().doubleValue())
                    .pricePerPlayer(pricePerPlayer)
                    .splitPayment(b.isSplitPayment())
                    .notes(b.getNotes())
                    .distanceKm(distanceKm != null ? Math.round(distanceKm * 10.0) / 10.0 : null)
                    .myRequestStatus(myRequest.map(JoinRequest::getStatus).orElse(null))
                    .ownerKarma(playerRepository.findByUser_IdUser(b.getOwner().getIdUser())
                                    .map(Player::getKarma).orElse(100))
                    .build();
        }).toList();

        // Reservas cuyo owner tiene karma < 60 van al final
        return results.stream()
                .sorted((a, b) -> {
                    boolean aRelegate = a.getOwnerKarma() < 60;
                    boolean bRelegate = b.getOwnerKarma() < 60;
                    if (aRelegate == bRelegate) return 0;
                    return aRelegate ? 1 : -1;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public com.corty.backend.dto.JoinPaymentCheckResponse joinPaymentCheck(Long bookingId, String username) {
        Player player = resolvePlayer(username);
        Booking booking = resolveBooking(bookingId);
        boolean requiresPayment = booking.isSplitPayment();
        int maxPlayers = booking.getCourt().getSport().getPlayersPerMatch();
        java.math.BigDecimal splitPrice = booking.getTotalPrice()
                .divide(java.math.BigDecimal.valueOf(maxPlayers), 2, RoundingMode.HALF_UP);
        return com.corty.backend.dto.JoinPaymentCheckResponse.builder()
                .requiresPayment(requiresPayment)
                .hasPaymentMethod(player.getDefaultPaymentMethodId() != null)
                .amount(requiresPayment ? splitPrice : java.math.BigDecimal.ZERO)
                .build();
    }

    @Transactional
    public void sendJoinRequest(Long bookingId, String username) {
        Player player = resolvePlayer(username);
        Booking booking = resolveBooking(bookingId);

        if (booking.getBookingType() != BookingType.PUBLIC) {
            throw new BusinessLogicException("Esta reserva no es pública");
        }
        if (player.getKarma() < 15) {
            throw new BusinessLogicException("Tu karma es demasiado bajo para unirte a partidos públicos. Juega partidos privados para recuperarlo.");
        }
        if (booking.isSplitPayment() && player.getDefaultPaymentMethodId() == null) {
            throw new BusinessLogicException("Necesitas añadir un método de pago antes de unirte a este partido.");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED || booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new BusinessLogicException("No puedes unirte a esta reserva");
        }
        if (booking.getOwner().getIdUser().equals(player.getUser().getIdUser())) {
            throw new BusinessLogicException("Eres el propietario de esta reserva");
        }

        int maxPlayers = booking.getCourt().getSport().getPlayersPerMatch();
        if (booking.getParticipants().size() >= maxPlayers) {
            throw new BusinessLogicException("La reserva está completa");
        }

        boolean alreadyParticipant = booking.getParticipants().stream()
                .anyMatch(pb -> pb.getPlayer().getIdPlayer().equals(player.getIdPlayer()));
        if (alreadyParticipant) {
            throw new BusinessLogicException("Ya eres participante de esta reserva");
        }

        if (joinRequestRepository.existsByBooking_IdBookingAndPlayer_IdPlayerAndStatus(bookingId, player.getIdPlayer(), JoinRequestStatus.PENDING)) {
            throw new BusinessLogicException("Ya tienes una petición pendiente para esta reserva");
        }

        JoinRequest request = JoinRequest.builder()
                .booking(booking)
                .player(player)
                .build();
        joinRequestRepository.save(request);

        // Notificar al owner de la reserva
        String requesterName = player.getName() + " " + player.getSurname();
        notificationService.send(
                booking.getOwner().getIdUser(),
                NotificationType.JOIN_REQUEST,
                "Nueva petición de unión",
                requesterName + " quiere unirse a tu reserva en " + booking.getCourt().getClub().getName(),
                booking.getIdBooking()
        );
    }

    @Transactional
    public void cancelJoinRequest(Long bookingId, String username) {
        Player player = resolvePlayer(username);
        JoinRequest request = joinRequestRepository.findByPlayerAndBooking(player.getIdPlayer(), bookingId)
                .orElseThrow(() -> new BusinessLogicException("No tienes ninguna petición para esta reserva"));
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new BusinessLogicException("Solo puedes cancelar peticiones pendientes");
        }
        joinRequestRepository.delete(request);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getPendingRequests(Long bookingId, String username) {
        Player owner = resolvePlayer(username);
        Booking booking = resolveBooking(bookingId);

        if (!booking.getOwner().getIdUser().equals(owner.getUser().getIdUser())) {
            throw new AccessDeniedException("Solo el propietario puede ver las peticiones");
        }

        return joinRequestRepository.findByBookingIdAndStatus(bookingId, JoinRequestStatus.PENDING)
                .stream()
                .map(jr -> {
                    Player p = jr.getPlayer();
                    double level = p.getSportsProfiles().stream()
                            .filter(ps -> ps.getSport().getIdSport().equals(booking.getCourt().getSport().getIdSport()))
                            .mapToDouble(ps -> ps.getLevel())
                            .average()
                            .orElse(0.0);
                    return JoinRequestResponse.builder()
                            .id(jr.getIdJoinRequest())
                            .playerId(p.getIdPlayer())
                            .name(p.getName())
                            .surname(p.getSurname())
                            .avatarUrl(p.getAvatarUrl())
                            .level(Math.round(level * 10.0) / 10.0)
                            .karma(p.getKarma())
                            .status(jr.getStatus())
                            .build();
                }).toList();
    }

    @Transactional
    public void acceptJoinRequest(Long bookingId, Long requestId, String username) {
        Player owner = resolvePlayer(username);
        Booking booking = resolveBooking(bookingId);

        if (!booking.getOwner().getIdUser().equals(owner.getUser().getIdUser())) {
            throw new AccessDeniedException("Solo el propietario puede aceptar peticiones");
        }

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Petición no encontrada"));

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new BusinessLogicException("Esta petición ya fue procesada");
        }

        int maxPlayers = booking.getCourt().getSport().getPlayersPerMatch();
        if (booking.getParticipants().size() >= maxPlayers) {
            throw new BusinessLogicException("La reserva ya está completa");
        }

        request.setStatus(JoinRequestStatus.ACCEPTED);
        joinRequestRepository.save(request);

        // Calcular splitPrice actualizado para todos
        int newCount = booking.getParticipants().size() + 1;
        BigDecimal splitPrice = booking.getTotalPrice()
                .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        // Actualizar splitPrice de participantes existentes
        booking.getParticipants().forEach(pb -> {
            pb.setSplitPrice(splitPrice);
            playerBookingRepository.save(pb);
        });

        // Cobrar al jugador si splitPayment=true usando precio mínimo (totalPrice / playersPerMatch)
        ensurePlayerSportExists(request.getPlayer(), booking.getCourt().getSport());
        boolean charged = false;
        String paymentIntentId = null;
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (booking.isSplitPayment()) {
            paidAmount = booking.getTotalPrice()
                    .divide(BigDecimal.valueOf(maxPlayers), 2, RoundingMode.HALF_UP);
            String description = booking.getCourt().getName() + " · " + booking.getDate();
            paymentIntentId = stripeService.chargePlayer(request.getPlayer(), paidAmount, description);
            charged = true;
        }

        PlayerBooking pb = PlayerBooking.builder()
                .booking(booking)
                .player(request.getPlayer())
                .team(Team.NONE)
                .splitPrice(splitPrice)
                .paidAmount(charged ? paidAmount : null)
                .hasPaid(charged)
                .paymentMethod(charged ? com.corty.backend.model.enums.PaymentMethod.ONLINE : com.corty.backend.model.enums.PaymentMethod.CASH)
                .paidAt(charged ? java.time.LocalDateTime.now() : null)
                .paymentId(paymentIntentId)
                .build();
        playerBookingRepository.save(pb);

        String clubName = booking.getCourt().getClub().getName();
        String courtName = booking.getCourt().getName();
        String dateStr = booking.getDate().toString();
        String startTimeStr = booking.getStartTime().toString();
        Long bookingId2 = booking.getIdBooking();
        String newPlayerName = request.getPlayer().getName() + " " + request.getPlayer().getSurname();

        // Notificar y enviar email al jugador aceptado
        notificationService.send(
                request.getPlayer().getUser().getIdUser(),
                NotificationType.JOIN_ACCEPTED,
                "Petición aceptada",
                "Tu petición para unirte a la reserva en " + clubName + " fue aceptada",
                bookingId2
        );
        emailService.sendJoinAccepted(
                request.getPlayer().getUser().getEmail(),
                request.getPlayer().getName(),
                courtName, clubName, dateStr, startTimeStr
        );

        Long ownerUserId = booking.getOwner().getIdUser();
        Long newPlayerUserId = request.getPlayer().getUser().getIdUser();

        // Notificar al owner
        notificationService.send(
                ownerUserId,
                NotificationType.PARTICIPANT_JOINED,
                "Nuevo participante",
                newPlayerName + " se ha unido a la reserva en " + clubName,
                bookingId2
        );

        // Notificar a los demás participantes (excluir owner y al jugador recién aceptado)
        booking.getParticipants().stream()
                .filter(p -> !p.getPlayer().getUser().getIdUser().equals(ownerUserId))
                .filter(p -> !p.getPlayer().getUser().getIdUser().equals(newPlayerUserId))
                .forEach(existingPb -> notificationService.send(
                        existingPb.getPlayer().getUser().getIdUser(),
                        NotificationType.PARTICIPANT_JOINED,
                        "Nuevo participante",
                        newPlayerName + " se ha unido a la reserva en " + clubName,
                        bookingId2
                ));

        // Si el partido está completo, notificar a todos (MATCH_READY) + email
        int totalAfterJoin = booking.getParticipants().size() + 1;
        if (totalAfterJoin >= maxPlayers) {
            // Incluye al nuevo jugador (aún no está en booking.getParticipants() en memoria)
            booking.getParticipants().forEach(existingPb -> {
                notificationService.send(
                        existingPb.getPlayer().getUser().getIdUser(),
                        NotificationType.MATCH_READY,
                        "¡Partido completo!",
                        "El partido en " + clubName + " ya tiene todos los jugadores",
                        bookingId2
                );
            });
            // Notificar también al nuevo jugador (aún no en la lista)
            notificationService.send(
                    newPlayerUserId,
                    NotificationType.MATCH_READY,
                    "¡Partido completo!",
                    "El partido en " + clubName + " ya tiene todos los jugadores",
                    bookingId2
            );
        }
    }

    @Transactional
    public void rejectJoinRequest(Long bookingId, Long requestId, String username) {
        Player owner = resolvePlayer(username);
        Booking booking = resolveBooking(bookingId);

        if (!booking.getOwner().getIdUser().equals(owner.getUser().getIdUser())) {
            throw new AccessDeniedException("Solo el propietario puede rechazar peticiones");
        }

        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Petición no encontrada"));

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new BusinessLogicException("Esta petición ya fue procesada");
        }

        request.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(request);

        String clubName = booking.getCourt().getClub().getName();
        // Notificar al jugador rechazado
        notificationService.send(
                request.getPlayer().getUser().getIdUser(),
                NotificationType.JOIN_REJECTED,
                "Petición rechazada",
                "Tu petición para unirte a la reserva en " + clubName + " fue rechazada",
                booking.getIdBooking()
        );
        emailService.sendJoinRejected(
                request.getPlayer().getUser().getEmail(),
                request.getPlayer().getName(),
                booking.getCourt().getName(),
                clubName,
                booking.getDate().toString(),
                booking.getStartTime().toString()
        );
    }

    private Player resolvePlayer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de jugador no encontrado"));
    }

    private Booking resolveBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private void ensurePlayerSportExists(Player player, Sport sport) {
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
}
