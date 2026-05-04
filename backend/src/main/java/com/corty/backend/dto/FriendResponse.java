package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendResponse {
    private Long friendshipId;
    private Long playerId;
    private String name;
    private String surname;
    private String avatarUrl;
    private Integer karma;
    private String status; // PENDING | ACCEPTED
    private boolean iAmRequester;
}
