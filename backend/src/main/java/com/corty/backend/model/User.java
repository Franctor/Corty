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
    @Builder.Default
    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private Role role;

    @JsonIgnore
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_authorities",
        joinColumns = @JoinColumn(name = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "id_authority")
    )
    private Set<Authority> extraAuthorities = new HashSet<>();

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ActivationToken activationToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> granted = new HashSet<>();

        if (role != null) {
            granted.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        if (extraAuthorities != null) {
            extraAuthorities.forEach(a -> granted.add(new SimpleGrantedAuthority(a.getName())));
        }

        return granted;
    }

    @Override
    public boolean isAccountNonExpired() {
        if (expiryDate == null) return true;
        return LocalDateTime.now().isBefore(expiryDate);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
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
