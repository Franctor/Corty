package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horaries_clubs")
public class HoraryClub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horary_club")
    private Long idHoraryClub;
    @Enumerated(EnumType.STRING)
    @Column(name = "day_week", nullable = false)
    private DayOfWeek dayWeek;
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;

    @PrePersist
    @PreUpdate
    public void validateTimes() {
        if (!isClosed && closeTime != null && openTime != null) {
            if (closeTime.isBefore(openTime)) {
                throw new IllegalStateException("Close time must be after open time");
            }
        }
    }
}
