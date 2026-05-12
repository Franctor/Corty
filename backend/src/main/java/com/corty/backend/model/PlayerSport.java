package com.corty.backend.model;

import com.corty.backend.model.enums.SportDominantSide;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_sports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_player", "id_sport"})
})
public class PlayerSport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_player_sport")
    private Long idPlayerSport;
    @Builder.Default
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    @Column(name = "level", nullable = false)
    private Double level = 0.0;
    @Builder.Default
    @Min(value = 0)
    @Column(name = "played_matches", nullable = false)
    private Integer playedMatches = 0;
    @Builder.Default
    @Min(value = 0)
    @Column(name = "wins", nullable = false)
    private Integer wins = 0;
    @Builder.Default
    @Min(value = 0)
    @Column(name = "losses", nullable = false)
    private Integer losses = 0;
    @Column(name = "last_level_change")
    @UpdateTimestamp
    private LocalDateTime lastLevelChange;
    @Enumerated(EnumType.STRING)
    private SportDominantSide dominantSide;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "player_sport_positions",
            joinColumns = @JoinColumn(name = "id_player_sport"),
            inverseJoinColumns = @JoinColumn(name = "id_position")
    )
    private List<SportPosition> positions = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player", nullable = false)
    private Player player;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;
}
