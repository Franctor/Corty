package com.corty.backend.dto;

import com.corty.backend.model.enums.JoinRequestStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRequestResponse {

    private Long id;
    private Long playerId;
    private String name;
    private String surname;
    private String avatarUrl;
    private double level;
    private int karma;
    private JoinRequestStatus status;
}
