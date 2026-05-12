package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organization")
    private Long idOrganization;
    @Column(name = "business_name", nullable = true, unique = true, length = 100)
    private String businessName;
    @Column(name = "cif", nullable = true, unique = true, length = 9)
    private String cif;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_user", unique = true)
    private User user;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Club> clubs = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_city")
    private City fiscalCity;
}
