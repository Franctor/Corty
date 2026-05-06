package com.corty.backend.service;

import com.corty.backend.model.Booking;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.services.CancellationPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CancellationPolicy — ventanas y penalizaciones de karma")
class CancellationPolicyTest {

    private Booking bookingInHours(long hoursFromNow, boolean splitPayment) {
        LocalDateTime start = LocalDateTime.now().plusHours(hoursFromNow);
        return Booking.builder()
                .date(start.toLocalDate())
                .startTime(start.toLocalTime())
                .endTime(start.plusHours(1).toLocalTime())
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .splitPayment(splitPayment)
                .totalPrice(BigDecimal.valueOf(20))
                .build();
    }

    // ── Ventana FREE (> 24h) ──────────────────────────────────────────────────

    @Test
    @DisplayName("TC-B01: Más de 24h → ventana FREE, sin penalización")
    void free_window_no_karma_penalty() {
        Booking booking = bookingInHours(48, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getWindow()).isEqualTo(CancellationPolicy.Window.FREE);
        assertThat(policy.getKarmaPenalty()).isZero();
    }

    @Test
    @DisplayName("TC-B02: Más de 24h → refund info menciona reembolso completo")
    void free_window_refund_message() {
        Booking booking = bookingInHours(48, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getRefundInfo(true)).contains("reembolso completo");
    }

    // ── Ventana PARTIAL (2–24h) ───────────────────────────────────────────────

    @Test
    @DisplayName("TC-B03: Entre 2h y 24h → ventana PARTIAL, -5 karma")
    void partial_window_5_karma_penalty() {
        Booking booking = bookingInHours(12, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getWindow()).isEqualTo(CancellationPolicy.Window.PARTIAL);
        assertThat(policy.getKarmaPenalty()).isEqualTo(5);
    }

    @Test
    @DisplayName("TC-B04: Ventana PARTIAL → refund info menciona 50%")
    void partial_window_refund_message() {
        Booking booking = bookingInHours(12, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getRefundInfo(true)).contains("50%");
    }

    // ── Ventana NO_REFUND (< 2h) ──────────────────────────────────────────────

    @Test
    @DisplayName("TC-B05: Menos de 2h → ventana NO_REFUND, -15 karma")
    void no_refund_window_15_karma_penalty() {
        Booking booking = Booking.builder()
                .date(LocalDate.now())
                .startTime(LocalTime.now().plusMinutes(30))
                .endTime(LocalTime.now().plusMinutes(90))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .splitPayment(true)
                .totalPrice(BigDecimal.valueOf(20))
                .build();
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getWindow()).isEqualTo(CancellationPolicy.Window.NO_REFUND);
        assertThat(policy.getKarmaPenalty()).isEqualTo(15);
    }

    // ── splitPayment=false: participante sin reembolso ────────────────────────

    @Test
    @DisplayName("TC-B06: splitPayment=false → participante no recibe reembolso aunque esté en ventana FREE")
    void no_split_participant_gets_no_refund() {
        Booking booking = bookingInHours(48, false);
        CancellationPolicy policy = new CancellationPolicy(booking);

        String refundInfo = policy.getRefundInfo(false);
        assertThat(refundInfo).contains("El propietario pagó la reserva completa");
    }

    @Test
    @DisplayName("TC-B07: splitPayment=true y ventana FREE → participante recibe reembolso completo")
    void split_participant_gets_full_refund_in_free_window() {
        Booking booking = bookingInHours(48, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        String refundInfo = policy.getRefundInfo(false);
        assertThat(refundInfo).contains("reembolso completo");
    }

    // ── getCancellationMessage ────────────────────────────────────────────────

    @Test
    @DisplayName("TC-B08: Owner cancela → mensaje empieza por 'La reserva ha sido cancelada'")
    void owner_cancellation_message() {
        Booking booking = bookingInHours(48, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getCancellationMessage(true)).startsWith("La reserva ha sido cancelada");
    }

    @Test
    @DisplayName("TC-B09: Participante abandona → mensaje empieza por 'Has abandonado'")
    void participant_leave_message() {
        Booking booking = bookingInHours(48, true);
        CancellationPolicy policy = new CancellationPolicy(booking);

        assertThat(policy.getCancellationMessage(false)).startsWith("Has abandonado");
    }
}
