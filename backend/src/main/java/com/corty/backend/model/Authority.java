package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "authorities")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Authority {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_authority")
    private Long idAuthority;

    @Column(unique = true, nullable = false)
    private String name;

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "authorities")
    private HashSet<Role> roles = new HashSet<>();
}
