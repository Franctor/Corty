package com.corty.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.corty.backend.model.enums.BookingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingListItemResponse {

    private Long id;
    private String courtName;
    private String clubName;
    private String clubLogoUrl;
    private String sport;
    private String sportIconUrl;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus bookingStatus;
    private double totalPrice;
    private boolean fullyPaid;
    private int participantCount;
}
