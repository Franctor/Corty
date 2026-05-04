package com.corty.backend.services;

import com.corty.backend.dto.NotificationResponse;
import com.corty.backend.model.Notification;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.repository.NotificationRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseService sseService;
    private final ApplicationEventPublisher eventPublisher;

    public void send(Long userId, NotificationType type, String title, String message, Long referenceId) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();
        Notification saved = notificationRepository.save(notification);
        eventPublisher.publishEvent(new SsePushEvent(userId, toResponse(saved)));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSsePush(SsePushEvent event) {
        sseService.push(event.userId(), event.payload());
    }

    public record SsePushEvent(Long userId, NotificationResponse payload) {}

    public List<NotificationResponse> getForCurrentUser(Long userId) {
        return notificationRepository.findByUserIdUserOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdUserAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getIdUser().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getIdNotification())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .build();
    }
}
