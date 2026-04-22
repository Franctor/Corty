package com.corty.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgAdminRequest {
    @NotBlank private String businessName;
    @NotBlank @Size(min = 9, max = 9) private String cif;
    private Long cityId;
}
