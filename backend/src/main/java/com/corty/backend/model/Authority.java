package com.corty.backend.model;

import jakarta.persistence.*;
import lombok.*;

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
}
