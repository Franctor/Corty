package com.corty.backend.dto;

import lombok.Data;

@Data
public class SurfaceResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
}
