package com.corty.backend.model;

import com.corty.backend.model.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
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
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;
    @Builder.Default
    @Min(0)
    @Max(100)
    @Column(name = "karma", nullable = false)
    private Integer karma = 100;
    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;
    @Column(name = "phone", nullable = false,unique = true, length = 20)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;
    @Past
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    @Column(name = "biography")
    private String biography;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_city")
    private City city;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_user", unique = true)
    private User user;

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "rater",cascade = CascadeType.ALL)
    private List<ClubReview> clubReviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ratedPlayer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PlayerReview> receivedRatings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "rater", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PlayerReview> givenRatings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlayerSport> sportsProfiles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "player")
    @JsonIgnore
    private List<PlayerBooking> participations = new ArrayList<>();
}
