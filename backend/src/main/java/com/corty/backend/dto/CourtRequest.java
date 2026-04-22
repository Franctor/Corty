package com.corty.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourtRequest {
    @NotBlank @Size(max = 50)
    private String name;
    @NotNull @DecimalMin("0.0")
    private BigDecimal pricePerHour;
    private boolean active = true;
    private boolean covered;
    private boolean lighting;
    @NotNull
    private Long clubId;
    @NotNull
    private Long sportId;
    private Long surfaceId;
    private String imageUrl;
    private boolean useClubSchedule = true;
    @Min(value = 15, message = "El slot mínimo es 15 minutos")
    private int slotDurationMinutes = 60;
}
