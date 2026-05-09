package com.corty.backend.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
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
        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (json != null && !json.isBlank()) {
            return GoogleCredentials
                    .fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");
        }
        return GoogleCredentials
                .fromStream(new ClassPathResource(credentialsPath).getInputStream())
                .createScoped("https://www.googleapis.com/auth/firebase.messaging");
    }

    public void sendPush(String fcmToken, String title, String body) {
        sendPush(fcmToken, title, body, null, null);
    }

    public void sendPush(String fcmToken, String title, String body, String tag) {
        sendPush(fcmToken, title, body, tag, null);
    }

    public void sendPush(String fcmToken, String title, String body, String tag, java.util.Map<String, String> data) {
        if (!initialized) { log.warn("[FCM] SDK not initialized, skipping push"); return; }
        if (fcmToken == null || fcmToken.isBlank()) { log.warn("[FCM] No FCM token for user, skipping push"); return; }
        log.info("[FCM] Sending push to token {}... title='{}'", fcmToken.substring(0, Math.min(20, fcmToken.length())), title);
        try {
            AndroidNotification.Builder androidNotif = AndroidNotification.builder()
                    .setTitle(title)
                    .setBody(body);
            if (tag != null) androidNotif.setTag(tag);

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(androidNotif.build())
                            .build());
            if (data != null) messageBuilder.putAllData(data);

            String messageId = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("[FCM] Push sent OK, messageId={}", messageId);
        } catch (Exception e) {
            log.error("[FCM] Send failed: {}", e.getMessage(), e);
        }
    }
}
