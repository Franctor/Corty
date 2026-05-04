package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetupIntentResponse {
    private String clientSecret;
    private String publishableKey;
}
