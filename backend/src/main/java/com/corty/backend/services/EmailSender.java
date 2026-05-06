package com.corty.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class EmailSender {

    private final RestClient restClient;

    @Value("${corty.mail.from}")
    private String from;

    public EmailSender(@Value("${corty.mail.resend-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Async
    public void send(String to, String subject, String htmlBody) {
        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", from,
                            "to", new String[]{to},
                            "subject", subject,
                            "html", htmlBody
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email enviado a {} via Resend", to);
        } catch (Exception e) {
            log.error("Error enviando email a {} via Resend: {}", to, e.getMessage());
        }
    }
}
