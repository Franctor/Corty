package com.corty.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SlotResponse {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
