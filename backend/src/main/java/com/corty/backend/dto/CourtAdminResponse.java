package com.corty.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CourtAdminResponse {

    private Long id;
    private String name;
    private BigDecimal pricePerHour;
    private boolean active;
    private boolean covered;
    private boolean lighting;
    private String clubName;
    private String clubCity;
    private String clubAddress;
    private String clubPhone;
    private String clubEmail;
    private String clubDescription;
    private String clubLogoUrl;
    private String sportName;
    private String surfaceName;
    private String imageUrl;
    private boolean useClubSchedule;
    private int slotDurationMinutes;
}
