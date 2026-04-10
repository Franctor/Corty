package com.corty.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

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
    private String cityName;
}
