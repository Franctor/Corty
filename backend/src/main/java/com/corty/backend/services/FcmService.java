package com.corty.backend.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class FcmService {

    @Value("${firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = loadCredentials();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
            }
            initialized = true;
            log.info("Firebase Admin SDK initialized");
        } catch (Exception e) {
            log.warn("Firebase Admin SDK not initialized (missing credentials): {}", e.getMessage());
        }
    }

    private GoogleCredentials loadCredentials() throws Exception {
        // Primero intentar variable de entorno (Railway/producción)
        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (json != null && !json.isBlank()) {
            return GoogleCredentials.fromStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
            );
        }
        // Fallback: fichero en classpath (desarrollo local)
        return GoogleCredentials.fromStream(
            new ClassPathResource(credentialsPath).getInputStream()
        );
    }

    public void sendPush(String fcmToken, String title, String body) {
        if (!initialized || fcmToken == null || fcmToken.isBlank()) return;
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().sendAsync(message);
        } catch (Exception e) {
            log.warn("FCM send failed for token {}: {}", fcmToken, e.getMessage());
        }
    }
}
