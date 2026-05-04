package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponse {
    private String clientSecret;
    private String publishableKey;
    private Long   bookingId;
    private String amount;      // formatted "22.00"
    private String description; // "Pista Pádel 1 · 24 abr 10:00"
}
