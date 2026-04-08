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
    private String surface;     // nullable — no todas las pistas tienen surface definida
    private BigDecimal pricePerHour;
    private double distance;    // km, calculado en la query
    private String coverType;   // "indoor" | "outdoor"
}
