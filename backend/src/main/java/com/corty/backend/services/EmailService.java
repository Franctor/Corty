package com.corty.backend.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
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
            mailSender.send(message);
            log.info("Email enviado a {}", to);
        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }

    public void sendActivation(String to, String username, String activationUrl) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("activationUrl", activationUrl);
        send(to, "Activa tu cuenta en Corty", render("email/activation", ctx));
    }

    public void sendWelcome(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        send(to, "Bienvenido a Corty, " + username, render("email/welcome", ctx));
    }

    public void sendAccountEnabled(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "activada");
        send(to, "Tu cuenta en Corty ha sido activada", render("email/account-enabled", ctx));
    }

    public void sendAccountUnlocked(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "desbloqueada");
        send(to, "Tu cuenta en Corty ha sido desbloqueada", render("email/account-enabled", ctx));
    }

    public void sendAccountDisabled(String to, String username, String reason) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "desactivada");
        ctx.setVariable("reason", reason);
        send(to, "Tu cuenta en Corty ha sido desactivada", render("email/account-disabled", ctx));
    }

    public void sendAccountLocked(String to, String username, String reason) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "bloqueada");
        ctx.setVariable("reason", reason);
        send(to, "Tu cuenta en Corty ha sido bloqueada", render("email/account-disabled", ctx));
    }

    public void sendBookingCancelled(String to, String username, String courtName,
            String clubName, String date, String startTime, String reason) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("courtName", courtName);
        ctx.setVariable("clubName", clubName);
        ctx.setVariable("date", date);
        ctx.setVariable("startTime", startTime);
        ctx.setVariable("reason", reason);
        send(to, "Tu reserva en " + clubName + " ha sido cancelada", render("email/booking-cancelled", ctx));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Context baseContext() {
        Context ctx = new Context();
        ctx.setVariable("year", Year.now().getValue());
        return ctx;
    }

    private String render(String template, Context ctx) {
        return templateEngine.process(template, ctx);
    }
}
