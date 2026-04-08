package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentActivityResponse {
    private Long id;
    private String sport;
    private String sportIconUrl;
    private String sportColor;
    private String description;
    private LocalDateTime date;
}
