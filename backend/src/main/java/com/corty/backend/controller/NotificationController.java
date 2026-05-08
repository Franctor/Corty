package com.corty.backend.controller;

import com.corty.backend.dto.NotificationResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.NotificationService;
import com.corty.backend.services.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal User principal) {
        return sseService.subscribe(principal.getIdUser());
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(notificationService.getForCurrentUser(principal.getIdUser()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(principal.getIdUser())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        notificationService.markRead(id, principal.getIdUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal User principal) {
        notificationService.markAllRead(principal.getIdUser());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal User principal,
            @RequestBody Map<String, String> body) {
        notificationService.saveFcmToken(principal.getIdUser(), body.get("token"));
        return ResponseEntity.noContent().build();
    }
}
