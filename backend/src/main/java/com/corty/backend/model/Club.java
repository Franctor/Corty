package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
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
    @Lob
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "phone", nullable = false, length = 20, unique = true)
    private String phone;
    @Column(name = "contact_email", nullable = false, length = 100, unique = true)
    private String contactEmail;
    @Column(name = "address", nullable = false, length = 100)
    private String address;
    @Column(name = "nif", nullable = false, length = 9, unique = true)
    private String nif;
    @Column(name = "geo_lat", precision = 10, scale = 8)
    private BigDecimal geoLat;
    @Column(name = "geo_long", precision = 11, scale = 8)
    private BigDecimal geoLong;
    @CreationTimestamp
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
    @JoinColumn(name = "id_organization", nullable = false)
    @JsonIgnore
    private Organization organization;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Court> courts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.description == null) {
            this.description = "Nuevo club en " + (city != null ? city.getLabel() : "Corty");
        }
    }
}
