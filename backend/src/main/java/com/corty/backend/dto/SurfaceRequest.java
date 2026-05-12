package com.corty.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SurfaceRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String description;

    private String iconUrl;
}
