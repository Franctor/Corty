package com.corty.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ClubResponse {

    private Long id;
    private String name;
    private String description;
    private String phone;
    private String contactEmail;
    private String address;
    private String nif;
    private String logoUrl;
    private BigDecimal geoLat;
    private BigDecimal geoLong;
    private Long cityId;
    private String cityName;
    private Long organizationId;
    private String organizationName;
}
