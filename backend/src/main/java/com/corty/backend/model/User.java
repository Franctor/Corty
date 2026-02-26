package com.corty.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "creation_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime creationDate;
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private Role role;

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    private Set<Friendship> sentRequests = new HashSet<>();

    @Builder.Default
    @JsonIgnore
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    private Set<Friendship> receivedRequests = new HashSet<>();

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Organization organization;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Player player;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            if (role.getAuthorities() != null) {
                role.getAuthorities().forEach(p -> {
                    authorities.add(new SimpleGrantedAuthority(p.getName()));
                });
            }
        }

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
