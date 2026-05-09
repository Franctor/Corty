package com.corty.backend.services;

import com.corty.backend.dto.BookingResultRequest;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.PlayerSport;
import com.corty.backend.model.Sport;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.model.enums.Team;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerSportRepository;
import com.corty.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingResultService {

    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final PlayerSportRepository playerSportRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public void registerResult(Long bookingId, BookingResultRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!booking.getOwner().getIdUser().equals(user.getIdUser())) {
            throw new AccessDeniedException("Solo el propietario puede registrar el resultado");
        }
        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new BusinessLogicException("Solo se puede registrar el resultado de reservas completadas");
        }
        if (booking.getResult() != null) {
            throw new BusinessLogicException("El resultado ya fue registrado");
        }

        Sport sport = booking.getCourt().getSport();
        boolean isTeamSport = sport.isTeamSport();

        // Aplicar asignaciones de equipo
        Map<Long, Team> teamByPlayerId = request.getAssignments().stream()
                .collect(Collectors.toMap(
                        a -> a.getPlayerId(),
                        a -> a.getTeam()
                ));

        booking.getParticipants().forEach(pb -> {
            Team assignedTeam = teamByPlayerId.getOrDefault(pb.getPlayer().getIdPlayer(), Team.NONE);
            pb.setTeam(assignedTeam);
            if (request.getWinnerTeam() != null && assignedTeam != Team.NONE) {
                pb.setWinner(assignedTeam == request.getWinnerTeam());
            }
            playerBookingRepository.save(pb);
        });

        booking.setResult(request.getResult());
        bookingRepository.save(booking);

        if (isTeamSport) {
            applyEloTeam(booking, sport, request.getWinnerTeam());
        } else {
            applyEloIndividual(booking, sport, request.getWinnerTeam());
        }

        // Notificar a todos los participantes
        Long bookingId2 = booking.getIdBooking();
        String clubName = booking.getCourt().getClub().getName();
        booking.getParticipants().forEach(pb ->
                notificationService.send(
                        pb.getPlayer().getUser().getIdUser(),
                        NotificationType.SYSTEM_ALERT,
                        "Resultado registrado",
                        "El resultado de tu partido en " + clubName + " ha sido registrado",
                        bookingId2
                )
        );
    }

    private void applyEloTeam(Booking booking, Sport sport, Team winnerTeam) {
        List<PlayerBooking> teamA = booking.getParticipants().stream()
                .filter(pb -> pb.getTeam() == Team.A).toList();
        List<PlayerBooking> teamB = booking.getParticipants().stream()
                .filter(pb -> pb.getTeam() == Team.B).toList();

        if (teamA.isEmpty() || teamB.isEmpty()) return;

        double avgLevelA = avgLevel(teamA, sport);
        double avgLevelB = avgLevel(teamB, sport);

        double scoreA = winnerTeam == null ? 0.5 : (winnerTeam == Team.A ? 1.0 : 0.0);
        double scoreB = 1.0 - scoreA;

        teamA.forEach(pb -> updatePlayerLevel(pb.getPlayer().getIdPlayer(), sport, avgLevelB, scoreA, winnerTeam == Team.A));
        teamB.forEach(pb -> updatePlayerLevel(pb.getPlayer().getIdPlayer(), sport, avgLevelA, scoreB, winnerTeam == Team.B));
    }

    private void applyEloIndividual(Booking booking, Sport sport, Team winnerTeam) {
        List<PlayerBooking> participants = booking.getParticipants();
        if (participants.size() < 2) return;

        // En deportes individuales Team.A = jugador 1, Team.B = jugador 2
        List<PlayerBooking> sideA = participants.stream().filter(pb -> pb.getTeam() == Team.A).toList();
        List<PlayerBooking> sideB = participants.stream().filter(pb -> pb.getTeam() == Team.B).toList();

        if (sideA.isEmpty() || sideB.isEmpty()) return;

        double levelA = getLevel(sideA.get(0).getPlayer().getIdPlayer(), sport);
        double levelB = getLevel(sideB.get(0).getPlayer().getIdPlayer(), sport);

        double scoreA = winnerTeam == null ? 0.5 : (winnerTeam == Team.A ? 1.0 : 0.0);
        double scoreB = 1.0 - scoreA;

        updatePlayerLevel(sideA.get(0).getPlayer().getIdPlayer(), sport, levelB, scoreA, winnerTeam == Team.A);
        updatePlayerLevel(sideB.get(0).getPlayer().getIdPlayer(), sport, levelA, scoreB, winnerTeam == Team.B);
    }

    private void updatePlayerLevel(Long playerId, Sport sport, double opponentLevel, double actualScore, boolean won) {
        PlayerSport playerSport = playerSportRepository
                .findByPlayer_IdPlayerAndSport_IdSport(playerId, sport.getIdSport())
                .orElse(null);
        if (playerSport == null) return;

        double currentLevel = playerSport.getLevel();

        // K-factor adaptativo: más cambio si pocos partidos jugados (nivel poco consolidado)
        double kFactor = playerSport.getPlayedMatches() < 10 ? 1.0 : (playerSport.getPlayedMatches() < 30 ? 0.6 : 0.3);

        // ELO adaptado a escala 0-10: expected score basado en diferencia de nivel
        double levelDiff = opponentLevel - currentLevel;
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, levelDiff / 4.0));

        double newLevel = currentLevel + kFactor * (actualScore - expectedScore);
        newLevel = Math.max(0.0, Math.min(10.0, newLevel));

        playerSport.setLevel(Math.round(newLevel * 10.0) / 10.0);
        playerSport.setPlayedMatches(playerSport.getPlayedMatches() + 1);
        if (won) {
            playerSport.setWins(playerSport.getWins() + 1);
        } else if (actualScore < 0.5) {
            playerSport.setLosses(playerSport.getLosses() + 1);
        }
        playerSportRepository.save(playerSport);
    }

    private double avgLevel(List<PlayerBooking> participants, Sport sport) {
        return participants.stream()
                .mapToDouble(pb -> getLevel(pb.getPlayer().getIdPlayer(), sport))
                .average()
                .orElse(5.0);
    }

    private double getLevel(Long playerId, Sport sport) {
        return playerSportRepository
                .findByPlayer_IdPlayerAndSport_IdSport(playerId, sport.getIdSport())
                .map(PlayerSport::getLevel)
                .orElse(5.0);
    }
}
