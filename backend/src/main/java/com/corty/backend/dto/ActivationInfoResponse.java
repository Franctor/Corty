package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

/** Datos que el frontend necesita para pre-rellenar el formulario de activación */
@Data
@Builder
public class ActivationInfoResponse {
    private String token;
    private String username;
    private String email;
}
