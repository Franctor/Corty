package com.corty.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PlayerAdminCreateRequest {

    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8)
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    private String phone;
    @NotNull
    private String gender;
    @NotNull
    private LocalDate birthDate;
    private String biography;
    private String avatarUrl;
    private Long cityId;
    /**
     * Si true, la cuenta se activa directamente sin email de verificación
     */
    private Boolean verified;
}
