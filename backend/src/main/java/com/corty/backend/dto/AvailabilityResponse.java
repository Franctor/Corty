package com.corty.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AvailabilityResponse {

    private boolean closed;
    private List<SlotResponse> slots;
}
