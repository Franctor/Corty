package com.corty.backend.dto;

import com.corty.backend.model.enums.PaymentMethod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class BookingPresencialRequest {

    @NotNull
    private Long courtId;

    @NotNull
    @FutureOrPresent
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    /** Solo CASH o CREDIT_CARD — sin flujo online */
    @NotNull
    private PaymentMethod paymentMethod;

    private String notes;
}
