package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerStatsResponse {

    private int totalMatches;
    private int totalWins;
    private int totalLosses;
    private int karma;
    private List<SportStat> sports;

    @Data
    @Builder
    public static class SportStat {

        private String sport;
        private String sportIconUrl;
        private String sportColor;
        private int matches;
        private int wins;
        private int losses;
        private double level;
    }
}
