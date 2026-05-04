package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class JoinPaymentCheckResponse {
    private boolean requiresPayment;
    private boolean hasPaymentMethod;
    private BigDecimal amount;
}
