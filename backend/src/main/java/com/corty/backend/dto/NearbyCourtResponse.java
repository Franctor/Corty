package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NearbyCourtResponse {
    private Long id;
    private String name;
    private String clubName;
    private String sport;
    private String surface;
    private BigDecimal pricePerHour;
    private double distance;
    private String coverType;   // "indoor" | "outdoor"
    private boolean covered;
    private boolean lighting;
    private String imageUrl;
    private String clubCity;
}
