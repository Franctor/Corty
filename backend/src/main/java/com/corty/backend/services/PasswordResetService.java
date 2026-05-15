package com.corty.backend.services;

import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.PasswordResetToken;
import com.corty.backend.model.User;
import com.corty.backend.repository.PasswordResetTokenRepository;
import com.corty.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${corty.app.url}")
    private String appUrl;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUser_IdUser(user.getIdUser());

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(2))
                    .build();
            tokenRepository.save(resetToken);

            String resetUrl = appUrl + "/auth/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordReset(user.getEmail(), user.getUsername(), resetUrl);
        });
        // Siempre responde igual para no revelar si el email existe
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o no encontrado"));

        if (resetToken.isUsed()) {
            throw new IllegalStateException("Este enlace ya ha sido utilizado");
        }
        if (resetToken.isExpired()) {
            throw new IllegalStateException("El enlace ha expirado. Solicita uno nuevo");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
