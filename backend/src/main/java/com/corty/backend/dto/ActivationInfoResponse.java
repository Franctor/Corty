package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

/** Datos que el frontend necesita para determinar el flujo de activación */
@Data
@Builder
public class ActivationInfoResponse {
    private String token;
    private String username;
    private String email;
    /** true = el admin registró solo el email; el usuario debe completar su perfil.
     *  false = la cuenta ya tiene datos completos; solo hay que confirmar el email. */
    private boolean needsProfile;
    /** Tipo de usuario: "PLAYER" u "ORGANIZATION" */
    private String userType;
}
