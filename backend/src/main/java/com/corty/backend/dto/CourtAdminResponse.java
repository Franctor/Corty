package com.corty.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourtAdminResponse {
    private Long id;
    private String name;
    private BigDecimal pricePerHour;
    private boolean active;
    private boolean covered;
    private boolean lighting;
    private String clubName;
    private String sportName;
    private String surfaceName;
    private String imageUrl;
    private boolean useClubSchedule;
    private int slotDurationMinutes;
}
