package com.corty.backend.controller;

import com.corty.backend.dto.NotificationResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.NotificationService;
import com.corty.backend.services.SseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:4200", "http://localhost:4201", "http://localhost:8100",
            "https://corty-gilt.vercel.app", "https://admin-web-gold-eight.vercel.app",
            "capacitor://localhost", "https://localhost", "http://localhost"
    );

    private final NotificationService notificationService;
    private final SseService sseService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal User principal,
            HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no"); // evita que nginx/railway bufferice SSE
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

    @GetMapping("/prefs")
    public ResponseEntity<Map<String, Boolean>> getPrefs(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(notificationService.getNotifPrefs(principal.getIdUser()));
    }

    @PutMapping("/prefs")
    public ResponseEntity<Void> savePrefs(
            @AuthenticationPrincipal User principal,
            @RequestBody Map<String, Boolean> prefs) {
        notificationService.saveNotifPrefs(principal.getIdUser(), prefs);
        return ResponseEntity.noContent().build();
    }
}
