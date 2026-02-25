package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_city")
    private Long idCity;
    @Column(name = "code_city", nullable = false)
    private String code;
    @Column(name = "label", nullable = false)
    private String label;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code")
    private Province province;

    @JsonIgnore
    @OneToMany(mappedBy = "city")
    private List<Player> players;
    @JsonIgnore
    @OneToMany(mappedBy = "city")
    private List<Club> clubs;

    @JsonIgnore
    @OneToMany(mappedBy = "fiscalCity")
    private List<Organization> organizations;
}
