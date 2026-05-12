package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PlayerProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String avatarUrl;
    private String biography;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private Integer karma;
    private String city;
    private boolean publicProfile;
    private List<SportProfileResponse> sports;

    @Data
    @Builder
    public static class SportProfileResponse {

        private Long sportId;
        private String sport;
        private String sportIconUrl;
        private String sportColor;
        private Double level;
        private Integer playedMatches;
        private Integer wins;
        private Integer losses;
    }
}
