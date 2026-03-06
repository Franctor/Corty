package com.corty.backend.dto;

import com.corty.backend.model.enums.Gender;
import com.corty.backend.validation.MinAge;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPlayerRequest {

    // --- User credentials ---
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^\\S+$", message = "El nombre de usuario no puede contener espacios")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
            message = "La contraseña debe contener al menos una mayúscula, un número y un carácter especial"
    )
    private String password;

    // --- Player profile ---
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden superar los 50 caracteres")
    private String surname;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "El formato del teléfono no es válido")
    private String phone;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @MinAge(value = 18, message = "Debes ser mayor de 18 años para registrarte")
    private LocalDate birthDate;

    @Size(max = 500, message = "La biografía no puede superar los 500 caracteres")
    private String biography;

    // --- Location ---
    @NotNull(message = "La localidad es obligatoria")
    private Long cityId;

    // --- Avatar ---
    private String avatarUrl;
}