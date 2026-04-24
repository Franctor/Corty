package com.corty.backend.dto;

import com.corty.backend.model.enums.BookingType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingCreateRequest {
    @NotNull
    private Long courtId;
    @NotNull
    @FutureOrPresent
    private LocalDate date;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private BookingType bookingType;
    private boolean splitPayment = true;
    private String notes;
}
