package com.corty.backend.services;

import com.corty.backend.dto.NotificationResponse;
import com.corty.backend.model.Notification;
import com.corty.backend.model.User;
import com.corty.backend.model.UserNotificationPref;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.repository.NotificationRepository;
import com.corty.backend.repository.UserNotificationPrefRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationPrefRepository prefRepository;
    private final UserRepository userRepository;
    private final SseService sseService;
    private final FcmService fcmService;
    private final ApplicationEventPublisher eventPublisher;

    public void send(Long userId, NotificationType type, String title, String message, Long referenceId) {
        send(userId, type, title, message, referenceId, true);
    }

    public void send(Long userId, NotificationType type, String title, String message, Long referenceId, boolean sendFcm) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();
        Notification saved = notificationRepository.save(notification);
        eventPublisher.publishEvent(new SsePushEvent(
                userId, toResponse(saved), sendFcm ? user.getFcmToken() : null, title, message, null, type));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSsePush(SsePushEvent event) {
        sseService.push(event.userId(), event.payload());
        if (event.fcmToken() != null && isPushEnabled(event.userId(), event.type())) {
            Map<String, String> data = new java.util.HashMap<>();
            if (event.type() != null) data.put("type", event.type().name());
            Long refId = event.payload().getReferenceId();
            if (refId != null) data.put("referenceId", refId.toString());
            fcmService.sendPush(event.fcmToken(), event.title(), event.body(), event.tag(), data);
        }
    }

    public record SsePushEvent(
            Long userId, NotificationResponse payload,
            String fcmToken, String title, String body, String tag,
            NotificationType type) {}

    public void sendPushOnly(Long userId, String title, String body, String tag, NotificationType type) {
        sendPushOnly(userId, title, body, tag, type, null);
    }

    public void sendPushOnly(Long userId, String title, String body, String tag, NotificationType type, Long referenceId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFcmToken() != null && isPushEnabled(userId, type)) {
                Map<String, String> data = new java.util.HashMap<>();
                if (type != null) data.put("type", type.name());
                if (referenceId != null) data.put("referenceId", referenceId.toString());
                fcmService.sendPush(user.getFcmToken(), title, body, tag, data);
            }
        });
    }

    private boolean isPushEnabled(Long userId, NotificationType type) {
        if (type == null) return true;
        return prefRepository.findByUserIdUserAndNotificationType(userId, type)
                .map(UserNotificationPref::isEnabled)
                .orElse(true); // sin registro = habilitado por defecto
    }

    public Map<String, Boolean> getNotifPrefs(Long userId) {
        return prefRepository.findByUserIdUser(userId).stream()
                .collect(Collectors.toMap(
                        p -> p.getNotificationType().name(),
                        UserNotificationPref::isEnabled));
    }

    @Transactional
    public void saveNotifPrefs(Long userId, Map<String, Boolean> prefs) {
        User user = userRepository.findById(userId).orElseThrow();
        prefs.forEach((typeName, enabled) -> {
            try {
                NotificationType type = NotificationType.valueOf(typeName);
                UserNotificationPref pref = prefRepository
                        .findByUserIdUserAndNotificationType(userId, type)
                        .orElse(UserNotificationPref.builder().user(user).notificationType(type).build());
                pref.setEnabled(enabled);
                prefRepository.save(pref);
            } catch (IllegalArgumentException ignored) { /* tipo desconocido, ignorar */ }
        });
    }

    @Transactional
    public void saveFcmToken(Long userId, String token) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(token);
            userRepository.save(user);
        });
    }

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
