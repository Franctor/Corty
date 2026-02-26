package com.corty.backend.model;

import com.corty.backend.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Long idNotification;
    @Column(name = "title",nullable = false)
    private String title;
    @Column(name = "message",nullable = false, length = 500)
    private String message;
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
