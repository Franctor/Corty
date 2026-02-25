package com.corty.backend.model;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Table(name = "player_sports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_player", "id_sport"}) //Para que un jugador no tenga dos perfiles del mismo deporte
})
public class PlayerSport {
}
