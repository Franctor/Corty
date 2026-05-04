package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedCardResponse {
    private String brand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;
    private String paymentMethodId;
}
