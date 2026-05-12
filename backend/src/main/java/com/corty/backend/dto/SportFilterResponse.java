package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SportFilterResponse {

    private Long id;
    private String name;
    private String iconUrl;
    private String color;
}
