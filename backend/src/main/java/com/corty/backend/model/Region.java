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
@Table(name = "regions")
public class Region {

    @Id
    @Column(name = "region_code")
    private String code;
    @Column(name = "label", nullable = false)
    private String label;

    @JsonIgnore
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<Province> provinces;
}
