package com.corty.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentIntentRequest {
    @NotNull
    private Long bookingId;
}
