package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sports")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sport")
    private Long idSport;
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;
    @Min(value = 0)
    @Column(name = "players_per_team", nullable = false)
    private Integer playersPerTeam;
    @Min(value = 1)
    @Builder.Default
    @Column(name = "players_per_match", nullable = false)
    private Integer playersPerMatch = 1;
    @Column(name = "icon_url", nullable = false)
    private String iconUrl;
    @Column(name = "color", length = 7)
    private String color;
    @Column(name = "is_team_sport", nullable = false)
    private boolean teamSport;
    @Builder.Default
    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SportPosition> availablePositions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sport")
    @JsonIgnore
    private List<PlayerSport> playersProfiles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sport")
    @JsonIgnore
    private List<Court> courts = new ArrayList<>();
}
