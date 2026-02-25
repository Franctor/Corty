package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clubs")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_club")
    private Long idClub;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "phone",  nullable = false, length = 20, unique = true)
    private String phone;
    @Column(name = "contact_email", nullable = false, length = 100, unique = true)
    private String contactEmail;
    @Column(name = "address", nullable = false, length = 100)
    private String address;
    @Column(name = "nif", nullable = false, length = 9, unique = true)
    private String nif;
    @Column(name = "geo_lat", columnDefinition = "DECIMAL(10,8)")
    private Double geoLat;
    @Column(name = "geo_long", columnDefinition = "DECIMAL(11,8)")
    private Double geoLong;
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_city", nullable = false)
    private City city;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoraryClub> schedules = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organization")
    @JsonIgnore
    private Organization organization;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ClubReview> reviews = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.description == null) this.description = "Nuevo club en " + (city != null ? city.getLabel() : "Corty");
    }
}
