package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sport_positions")
public class SportPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sport_position")
    private Long idPosition;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "description", length = 50)
    private String description;
    @Column(name = "id_main_position", nullable = false)
    private Long idMainPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    @JsonIgnore
    private Sport sport;

    @ManyToMany(mappedBy = "positions")
    @JsonIgnore
    private List<PlayerSport> playerProfiles;
}
