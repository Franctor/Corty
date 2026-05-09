package com.corty.backend.model;

import com.corty.backend.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_notification_prefs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "notification_type"}))
public class UserNotificationPref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
