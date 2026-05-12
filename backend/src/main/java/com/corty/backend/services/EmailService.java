package com.corty.backend.services;

import java.time.Year;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    public void sendActivation(String to, String username, String activationUrl) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("activationUrl", activationUrl);
        emailSender.send(to, "Activa tu cuenta en Corty", render("email/activation", ctx));
    }

    public void sendWelcome(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        emailSender.send(to, "Bienvenido a Corty, " + username, render("email/welcome", ctx));
    }

    public void sendAccountEnabled(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "activada");
        emailSender.send(to, "Tu cuenta en Corty ha sido activada", render("email/account-enabled", ctx));
    }

    public void sendAccountUnlocked(String to, String username) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "desbloqueada");
        emailSender.send(to, "Tu cuenta en Corty ha sido desbloqueada", render("email/account-enabled", ctx));
    }

    public void sendAccountDisabled(String to, String username, String reason) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "desactivada");
        ctx.setVariable("reason", reason);
        emailSender.send(to, "Tu cuenta en Corty ha sido desactivada", render("email/account-disabled", ctx));
    }

    public void sendAccountLocked(String to, String username, String reason) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("action", "bloqueada");
        ctx.setVariable("reason", reason);
        emailSender.send(to, "Tu cuenta en Corty ha sido bloqueada", render("email/account-disabled", ctx));
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
        emailSender.send(to, "Tu reserva en " + clubName + " ha sido cancelada", render("email/booking-cancelled", ctx));
    }

    public void sendBookingConfirmed(String to, String username, String courtName,
            String clubName, String date, String startTime, String amount) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("courtName", courtName);
        ctx.setVariable("clubName", clubName);
        ctx.setVariable("date", date);
        ctx.setVariable("startTime", startTime);
        ctx.setVariable("amount", amount);
        emailSender.send(to, "¡Reserva confirmada en " + clubName + "!", render("email/booking-confirmed", ctx));
    }

    public void sendJoinAccepted(String to, String username, String courtName,
            String clubName, String date, String startTime) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("courtName", courtName);
        ctx.setVariable("clubName", clubName);
        ctx.setVariable("date", date);
        ctx.setVariable("startTime", startTime);
        emailSender.send(to, "¡Tu petición en " + clubName + " ha sido aceptada!", render("email/join-accepted", ctx));
    }

    public void sendJoinRejected(String to, String username, String courtName,
            String clubName, String date, String startTime) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("courtName", courtName);
        ctx.setVariable("clubName", clubName);
        ctx.setVariable("date", date);
        ctx.setVariable("startTime", startTime);
        emailSender.send(to, "Petición no aceptada en " + clubName, render("email/join-rejected", ctx));
    }

    public void sendResultPending(String to, String username, String courtName,
            String clubName, String date, String startTime) {
        Context ctx = baseContext();
        ctx.setVariable("username", username);
        ctx.setVariable("courtName", courtName);
        ctx.setVariable("clubName", clubName);
        ctx.setVariable("date", date);
        ctx.setVariable("startTime", startTime);
        emailSender.send(to, "Registra el resultado de tu partido en " + clubName, render("email/result-pending", ctx));
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
