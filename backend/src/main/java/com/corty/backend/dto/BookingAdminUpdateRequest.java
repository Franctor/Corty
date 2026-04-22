package com.corty.backend.dto;

import lombok.Data;

@Data
public class BookingAdminUpdateRequest {
    private String bookingStatus;
    private String notes;
    private String cancelReason;
}
