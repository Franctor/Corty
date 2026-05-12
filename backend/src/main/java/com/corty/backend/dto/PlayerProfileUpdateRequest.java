package com.corty.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlayerProfileUpdateRequest {

    @Size(max = 50)
    private String name;
    @Size(max = 50)
    private String surname;
    @Size(max = 500)
    private String biography;
    private String avatarUrl;
    private Long cityId;
    private boolean publicProfile;
}
