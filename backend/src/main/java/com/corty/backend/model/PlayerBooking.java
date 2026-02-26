package com.corty.backend.model;

import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.model.enums.Team;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_bookings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_player", "id_booking"})
})
public class PlayerBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_player_booking")
    private Long idPlayerBooking;
    @Enumerated(EnumType.STRING)
    @Column(name = "team",nullable = false)
    private Team team;
    @Column(name = "split_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal splitPrice;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    @Builder.Default
    @Column(name = "has_paid", nullable = false)
    private boolean hasPaid = false;
    @Builder.Default
    @Column(name = "is_confirmed",nullable = false)
    private boolean isConfirmed = false;
    @Column(name = "payment_id")
    private String paymentId;
    @Builder.Default
    @Column(name = "is_winner", nullable = false)
    private boolean isWinner = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_player", nullable = false)
    private Player player;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_booking", nullable = false)
    private Booking booking;
}
