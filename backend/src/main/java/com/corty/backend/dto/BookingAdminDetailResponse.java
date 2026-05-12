package com.corty.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingAdminDetailResponse {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String bookingType;
    private String bookingStatus;
    private BigDecimal totalPrice;
    private boolean fullyPaid;
    private boolean splitPayment;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;

    private String courtName;
    private String clubName;
    private String ownerUsername;

    private List<ParticipantRow> participants;

    @Data
    @Builder
    public static class ParticipantRow {

        private String username;
        private String fullName;
        private BigDecimal splitPrice;
        private boolean hasPaid;
        private boolean confirmed;
        private boolean winner;
        private boolean owner;
    }
}
