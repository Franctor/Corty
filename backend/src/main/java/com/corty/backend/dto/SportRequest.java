package com.corty.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SportRequest {
    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    @Min(0)
    private Integer playersPerTeam;

    @NotNull
    @Min(1)
    private Integer playersPerMatch;

    @NotBlank
    private String iconUrl;

    @Size(max = 7)
    private String color;

    @NotNull
    private Boolean teamSport;

    @Min(0)
    private Integer defaultDurationMins;
}
