package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingDetailResponse {

    private Long id;

    // Pista y club
    private String courtName;
    private String clubName;
    private String clubAddress;
    private String clubLogoUrl;
    private Double clubLat;
    private Double clubLng;

    // Deporte
    private String sport;
    private String sportIconUrl;

    // Fecha y hora
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    // Tipo y estado
    private String bookingType;   // "PRIVATE" | "PUBLIC"
    private String bookingStatus; // "PENDING" | "CONFIRMED" | "COMPLETED" | "CANCELLED"

    // Resultado (solo COMPLETED)
    private String result;
    private boolean hasWinners;

    // Pago
    private BigDecimal totalPrice;
    private BigDecimal courtPrice;
    private boolean fullyPaid;
    private boolean splitPayment;   // true = precio dividido entre participantes
    private String paymentMethod;

    // Usuario autenticado
    private boolean currentUserOwner;

    // Participantes
    private List<ParticipantResponse> participants;

    @Data
    @Builder
    public static class ParticipantResponse {
        private Long playerId;
        private String name;
        private String surname;
        private String avatarUrl;
        private String team;
        private BigDecimal splitPrice;
        private boolean hasPaid;
        private boolean confirmed;
        private boolean winner;
        private boolean owner;
        private boolean currentUser;
    }
}
