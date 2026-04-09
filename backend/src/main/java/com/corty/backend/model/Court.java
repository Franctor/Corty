package com.corty.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_court")
    private Long idCourt;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "price_per_hour", precision = 8, scale = 2, nullable = false)
    private BigDecimal pricePerHour;
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active=true;
    @Builder.Default
    @Column(name = "is_covered", nullable = false)
    private boolean covered=false;
    @Builder.Default
    @Column(name = "has_lighting", nullable = false)
    private boolean lighting=false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_club", nullable = false)
    private Club club;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_surface")
    private Surface surface;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sport", nullable = false)
    private Sport sport;
    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
