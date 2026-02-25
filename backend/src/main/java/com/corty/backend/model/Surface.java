package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "surfaces")
public class Surface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_surface")
    private Long idSurface;
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;
    @Column(name = "description", length = 100)
    private String description;
    @Column(name = "icon_url")
    private String iconUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "surface", cascade = CascadeType.ALL)
    private List<Court> courts;
}
