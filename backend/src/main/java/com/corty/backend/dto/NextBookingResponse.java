package com.corty.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class NextBookingResponse {

    private Long id;
    private String courtLabel;
    private String clubName;
    private LocalDate date;
    private LocalTime time;
    private String sport;
    // Participantes confirmados vs total esperado para el deporte
    private int confirmedPlayers;
    private int totalPlayers;
}
