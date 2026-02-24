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
@Table(name = "regions")
public class Region {
    @Id
    @Column(name = "region_code")
    private String code;
    @Column(name = "label", nullable = false)
    private String label;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<Province> provinces;
}
