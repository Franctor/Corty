package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_reviews")
public class PlayerReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_player_review")
    private Long idPlayerReview;
    @Column(name = "score", nullable = false)
    @Min(1)
    @Max(5)
    private Integer score;
    @Column(name = "comment")
    private String comment;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rater", nullable = false)
    @JsonIgnore
    private Player rater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rated_player", nullable = false)
    @JsonIgnore
    private Player ratedPlayer;
}
