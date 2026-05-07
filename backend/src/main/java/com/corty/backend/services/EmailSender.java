package com.corty.backend.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${corty.mail.from}")
    private String from;

    @Async
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from, "Corty");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            log.info("Intentando enviar correo a {}...", to);
            mailSender.send(message);
            log.info("Email enviado a {} via SMTP (Brevo)", to);
        } catch (Exception e) {
            log.error("Error enviando email a {} via SMTP: {}", to, e.getMessage());
        }
    }
}