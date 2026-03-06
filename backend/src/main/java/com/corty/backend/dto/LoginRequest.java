package com.corty.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username obligatorio")
    @Size(min = 3, max = 50, message = "Username tiene que tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "Contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña tiene que tener al menos 8 caracteres")
    private String password;
}