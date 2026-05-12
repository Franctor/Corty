package com.corty.backend.model;

import com.corty.backend.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_player")
    private Long idPlayer;
    @Column(name = "name", length = 50)
    private String name;
    @Column(name = "surname", length = 50)
    private String surname;
    @Builder.Default
    @Min(0)
    @Max(100)
    @Column(name = "karma", nullable = false)
    private Integer karma = 100;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "phone", unique = true, length = 20)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "profile_complete", nullable = false)
    @Builder.Default
    private boolean profileComplete = false;
    @Column(name = "biography")
    private String biography;
    @Builder.Default
    @Column(name = "public_profile", nullable = false)
    private boolean publicProfile = true;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "default_payment_method_id")
    private String defaultPaymentMethodId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_city")
    private City city;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", unique = true)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlayerSport> sportsProfiles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "player")
    @JsonIgnore
    private List<PlayerBooking> participations = new ArrayList<>();
}
