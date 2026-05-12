package com.corty.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PlayerAdminRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    private String phone;
    @NotNull
    private String gender;
    @NotNull
    @Past
    private LocalDate birthDate;
    private String biography;
    private String avatarUrl;
    @Min(0)
    @Max(100)
    private Integer karma;
    private Long cityId;
}
