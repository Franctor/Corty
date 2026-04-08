package com.corty.backend.services;

import com.corty.backend.model.Booking;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Política de cancelación de Corty:
 *  > 24h antes  → reembolso completo, 0 karma
 *  2–24h antes  → reembolso parcial (50%), -5 karma
 *  < 2h antes   → sin reembolso, -15 karma
 *
 *  Si splitPayment=false (owner pagó el total), los participantes no reciben reembolso
 *  al abandonar pero sí se les aplica penalización de karma.
 */
public class CancellationPolicy {

    public enum Window { FREE, PARTIAL, NO_REFUND }

    private final long hoursUntilBooking;
    private final boolean splitPayment;

    public CancellationPolicy(Booking booking) {
        LocalDateTime bookingStart = LocalDateTime.of(booking.getDate(), booking.getStartTime());
        this.hoursUntilBooking = ChronoUnit.HOURS.between(LocalDateTime.now(), bookingStart);
        this.splitPayment = booking.isSplitPayment();
    }

    public Window getWindow() {
        if (hoursUntilBooking > 24)  return Window.FREE;
        if (hoursUntilBooking >= 2)  return Window.PARTIAL;
        return Window.NO_REFUND;
    }

    public int getKarmaPenalty() {
        return switch (getWindow()) {
            case FREE      -> 0;
            case PARTIAL   -> 5;
            case NO_REFUND -> 15;
        };
    }

    public String getRefundInfo(boolean isOwner) {
        if (!isOwner && !splitPayment) {
            return "El propietario pagó la reserva completa, no se aplica reembolso.";
        }
        return switch (getWindow()) {
            case FREE      -> "Recibirás el reembolso completo en los próximos días.";
            case PARTIAL   -> "Cancelación tardía: recibirás un reembolso del 50% del importe pagado.";
            case NO_REFUND -> "Cancelación con menos de 2 horas: no se aplica reembolso.";
        };
    }

    public String getCancellationMessage(boolean isOwner) {
        String who = isOwner ? "La reserva ha sido cancelada." : "Has abandonado la reserva.";
        return who + " " + getRefundInfo(isOwner);
    }
}
