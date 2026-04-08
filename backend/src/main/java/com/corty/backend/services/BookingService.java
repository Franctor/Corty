package com.corty.backend.services;

import com.corty.backend.dto.BookingDetailResponse;
import com.corty.backend.dto.CancellationResponse;
import com.corty.backend.dto.NextBookingResponse;
import com.corty.backend.dto.RecentActivityResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.BookingMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Player;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final BookingMapper bookingMapper;

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

        // Solo el propietario o un participante puede ver el detalle
        boolean isOwner = booking.getOwner().getIdUser().equals(user.getIdUser());
        boolean isParticipant = booking.getParticipants().stream()
                .anyMatch(pb -> pb.getPlayer().getIdPlayer().equals(player.getIdPlayer()));

        if (!isOwner && !isParticipant) {
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

        CancellationPolicy policy = new CancellationPolicy(booking);
        int penalty = policy.getKarmaPenalty();

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        applyKarmaPenalty(player, penalty);

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

        playerBookingRepository.delete(pb);
        applyKarmaPenalty(player, penalty);

        return CancellationResponse.builder()
                .message(policy.getCancellationMessage(false))
                .karmaDeducted(penalty)
                .karmaRemaining(player.getKarma())
                .refundInfo(policy.getRefundInfo(false))
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    private Booking resolveBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
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
