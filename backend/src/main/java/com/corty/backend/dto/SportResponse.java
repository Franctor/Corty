package com.corty.backend.dto;

import lombok.Data;

@Data
public class SportResponse {
    private Long id;
    private String name;
    private Integer playersPerTeam;
    private Integer playersPerMatch;
    private String iconUrl;
    private String color;
    private boolean teamSport;
}
