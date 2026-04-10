package com.corty.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

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
        String html = buildActivationEmail(username, activationUrl);
        send(to, "Activa tu cuenta en Corty", html);
    }

    public void sendWelcome(String to, String username) {
        String html = buildWelcomeEmail(username);
        send(to, "¡Bienvenido a Corty, " + username + "!", html);
    }

    // ── Templates ─────────────────────────────────────────────────────────────

    private String buildActivationEmail(String username, String activationUrl) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <!-- Header -->
                    <tr>
                      <td style="background:#58CC02;padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:900;letter-spacing:-0.5px;">Corty</h1>
                      </td>
                    </tr>
                    <!-- Body -->
                    <tr>
                      <td style="padding:40px;">
                        <h2 style="margin:0 0 16px;color:#1a1a1a;font-size:22px;font-weight:700;">¡Hola, %s! 👋</h2>
                        <p style="margin:0 0 16px;color:#555;font-size:15px;line-height:1.6;">
                          Tu cuenta en <strong>Corty</strong> ha sido creada. Para activarla y completar tu perfil, haz clic en el botón de abajo.
                        </p>
                        <p style="margin:0 0 32px;color:#888;font-size:13px;">
                          Este enlace expira en <strong>48 horas</strong>.
                        </p>
                        <table cellpadding="0" cellspacing="0" width="100%%">
                          <tr>
                            <td align="center">
                              <a href="%s"
                                 style="display:inline-block;background:#58CC02;color:#ffffff;text-decoration:none;
                                        font-size:15px;font-weight:700;padding:14px 36px;border-radius:9999px;">
                                Activar mi cuenta
                              </a>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:32px 0 0;color:#aaa;font-size:12px;text-align:center;">
                          Si no esperabas este email, puedes ignorarlo.<br/>
                          © 2025 Corty. Todos los derechos reservados.
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(username, activationUrl);
    }

    private String buildWelcomeEmail(String username) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"/></head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:'Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <tr>
                      <td style="background:#58CC02;padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:900;letter-spacing:-0.5px;">Corty</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:40px;">
                        <h2 style="margin:0 0 16px;color:#1a1a1a;font-size:22px;font-weight:700;">¡Bienvenido a Corty, %s! 🎉</h2>
                        <p style="margin:0 0 16px;color:#555;font-size:15px;line-height:1.6;">
                          Tu cuenta está activa. Ya puedes explorar pistas, reservar y conectar con otros jugadores.
                        </p>
                        <p style="margin:32px 0 0;color:#aaa;font-size:12px;text-align:center;">
                          © 2025 Corty. Todos los derechos reservados.
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(username);
    }
}
