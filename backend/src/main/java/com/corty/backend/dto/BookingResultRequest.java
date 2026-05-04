package com.corty.backend.dto;

import com.corty.backend.model.enums.Team;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingResultRequest {
    private String result;

    // null = empate / sin ganador
    private Team winnerTeam;

    @NotNull
    private List<PlayerTeamAssignment> assignments;

    @Data
    public static class PlayerTeamAssignment {
        @NotNull
        private Long playerId;
        @NotNull
        private Team team;
    }
}
