package com.corty.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClubRequest {

    @NotBlank
    @Size(max = 50)
    private String name;
    private String description;
    @NotBlank
    @Size(max = 20)
    private String phone;
    @NotBlank
    @Email
    @Size(max = 100)
    private String contactEmail;
    @NotBlank
    @Size(max = 100)
    private String address;
    @NotBlank
    @Size(max = 9)
    private String nif;
    private String logoUrl;
    private BigDecimal geoLat;
    private BigDecimal geoLong;
    @NotNull
    private Long cityId;
    @NotNull
    private Long organizationId;
}
