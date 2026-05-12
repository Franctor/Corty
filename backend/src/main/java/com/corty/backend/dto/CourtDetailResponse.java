package com.corty.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CourtDetailResponse {

    private Long id;
    private String name;
    private BigDecimal pricePerHour;
    private boolean covered;
    private boolean lighting;
    private int slotDurationMinutes;
    private String imageUrl;
    private String sportName;
    private String surfaceName;
    private String clubName;
    private String clubCity;
    private String clubAddress;
    private String clubPhone;
    private String clubEmail;
    private String clubDescription;
    private String clubLogoUrl;
}
