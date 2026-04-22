package com.corty.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {
    @NotNull
    private Boolean enabled;
    @NotNull
    private Boolean locked;
    private String reason;
}
