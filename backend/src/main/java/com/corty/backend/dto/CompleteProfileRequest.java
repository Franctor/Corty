package com.corty.backend.dto;

import com.corty.backend.model.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompleteProfileRequest {
    @NotBlank
    private String token;

    // Común: solo requerido cuando el username fue autogenerado (perfil incompleto)
    private String username;

    // Opcionales player: solo requeridos cuando needsProfile=true y userType=PLAYER
    private String name;
    private String surname;
    private String phone;
    private Gender gender;
    private LocalDate birthDate;
    private String biography;
    private Long cityId;
    private String password;

    // Opcionales organización: solo requeridos cuando needsProfile=true y userType=ORGANIZATION
    private String businessName;
    private String cif;
    private Long fiscalCityId;
}
