package com.corty.backend.jobs;

import com.corty.backend.model.Booking;
import com.corty.backend.model.Player;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.services.EmailService;
import com.corty.backend.services.NotificationService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCompletionJob {

    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final NotificationService notificationService;
    private final PlayerRepository playerRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void completeFinishedBookings() {
        List<Booking> finished = bookingRepository.findConfirmedPastEndTime(LocalDate.now(), LocalTime.now());
        if (!finished.isEmpty()) {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

            finished.forEach(booking -> {
                booking.setBookingStatus(BookingStatus.COMPLETED);
                String clubName = booking.getCourt().getClub().getName();
                String courtName = booking.getCourt().getName();
                String date = booking.getDate().format(dateFmt);
                String startTime = booking.getStartTime().format(timeFmt);
                Long bookingId = booking.getIdBooking();
                List<PlayerBooking> participants = booking.getParticipants();
                int actualCount = participants.size();

                // +2 karma a todos los participantes
                participants.forEach(pb -> {
                    Player p = pb.getPlayer();
                    int oldKarma = p.getKarma();
                    int newKarma = Math.min(100, oldKarma + 2);
                    p.setKarma(newKarma);
                    playerRepository.save(p);

                    // Notificar si el jugador sube de rango de karma
                    boolean crossedThreshold = (oldKarma < 35 && newKarma >= 35)
                            || (oldKarma < 60 && newKarma >= 60);
                    if (crossedThreshold) {
                        String newRange = newKarma >= 60 ? "libre" : "normal";
                        notificationService.send(
                                p.getUser().getIdUser(),
                                NotificationType.LEVEL_UP,
                                "¡Karma mejorado!",
                                "Tu karma ha subido a " + newKarma + " y tienes acceso " + newRange,
                                null
                        );
                    }
                });

                // Reembolso parcial si vinieron menos del máximo (solo splitPayment=true)
                if (booking.isSplitPayment() && actualCount > 0) {
                    BigDecimal fairShare = booking.getTotalPrice()
                            .divide(BigDecimal.valueOf(actualCount), 2, RoundingMode.HALF_UP);

                    participants.forEach(pb -> {
                        BigDecimal paid = pb.getPaidAmount();
                        if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) {
                            return;
                        }
                        BigDecimal refundAmount = paid.subtract(fairShare);
                        if (refundAmount.compareTo(BigDecimal.valueOf(0.50)) < 0) {
                            return; // mínimo 0.50€
                        }
                        try {
                            long refundCents = refundAmount
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(0, RoundingMode.HALF_UP)
                                    .longValue();
                            Refund.create(RefundCreateParams.builder()
                                    .setPaymentIntent(pb.getPaymentId())
                                    .setAmount(refundCents)
                                    .build());

                            pb.setPaidAmount(fairShare);
                            playerBookingRepository.save(pb);

                            notificationService.send(
                                    pb.getPlayer().getUser().getIdUser(),
                                    NotificationType.RESULT_PENDING,
                                    "Reembolso realizado",
                                    String.format("Se te han devuelto %.2f€ de tu reserva en %s", refundAmount.doubleValue(), clubName),
                                    bookingId
                            );
                        } catch (StripeException e) {
                            log.error("Error al reembolsar a jugador {}: {}", pb.getPlayer().getIdPlayer(), e.getMessage());
                        }
                    });
                }

                notificationService.send(
                        booking.getOwner().getIdUser(),
                        NotificationType.RESULT_PENDING,
                        "Partido finalizado",
                        "Tu partido en " + clubName + " ha terminado. ¿Cuál fue el resultado?",
                        bookingId
                );
                emailService.sendResultPending(
                        booking.getOwner().getEmail(),
                        booking.getOwner().getUsername(),
                        courtName, clubName, date, startTime
                );
            });

            bookingRepository.saveAll(finished);
            log.info("Marcadas {} reservas como COMPLETED", finished.size());
        }
    }
}
