package com.corty.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EmailSender {

    private final RestClient restClient;

    @Value("${corty.mail.from}")
    private String from;

    public EmailSender(@Value("${corty.mail.brevo-api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Async
    public void send(String to, String subject, String htmlBody) {
        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender",      Map.of("email", extractEmail(from), "name", extractName(from)),
                            "to",          List.of(Map.of("email", to)),
                            "subject",     subject,
                            "htmlContent", htmlBody
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email enviado a {} via Brevo API", to);
        } catch (Exception e) {
            log.error("Error enviando email a {} via Brevo API: {}", to, e.getMessage());
        }
    }

    private String extractName(String from) {
        int lt = from.indexOf('<');
        return lt > 0 ? from.substring(0, lt).trim() : from;
    }

    private String extractEmail(String from) {
        int lt = from.indexOf('<');
        int gt = from.indexOf('>');
        return (lt >= 0 && gt > lt) ? from.substring(lt + 1, gt).trim() : from;
    }
}
