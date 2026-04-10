package com.corty.backend.dto;

import com.corty.backend.model.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CompleteProfileRequest {
    @NotBlank private String token;
    @NotBlank private String name;
    @NotBlank private String surname;
    @NotBlank private String phone;
    @NotNull  private Gender gender;
    @NotNull  @Past private LocalDate birthDate;
    private String biography;
    private Long cityId;
}
