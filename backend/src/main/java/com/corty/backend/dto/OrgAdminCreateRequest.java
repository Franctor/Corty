package com.corty.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgAdminCreateRequest {

    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min = 8)
    private String password;
    @NotBlank
    private String businessName;
    @NotBlank
    @Size(min = 9, max = 9)
    private String cif;
    private Long cityId;
    /**
     * Si true, la cuenta se activa directamente sin email de verificación
     */
    private Boolean verified;
}
