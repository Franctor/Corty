package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingAdminResponse {
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
    private int participantCount;
}
