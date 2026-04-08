package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancellationResponse {
    private String message;       // Mensaje informativo para el usuario
    private int karmaDeducted;    // Karma restado (0, 5 o 15)
    private int karmaRemaining;   // Karma del jugador tras la penalización
    private String refundInfo;    // Texto informativo sobre reembolso
}
