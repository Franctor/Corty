package com.corty.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "provinces")
public class Province {
    @Id
    @Column(name = "province_code")
    private String code;
    @Column(name = "label", nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code")
    private Region region;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<City> cities;
}
