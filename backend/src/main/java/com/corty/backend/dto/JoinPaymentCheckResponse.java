package com.corty.backend.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinPaymentCheckResponse {

    private boolean requiresPayment;
    private boolean hasPaymentMethod;
    private BigDecimal amount;
}
