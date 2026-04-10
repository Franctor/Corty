package com.corty.backend.services;

import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.ActivationToken;
import com.corty.backend.model.User;
import com.corty.backend.repository.ActivationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationService {

    private final ActivationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${corty.app.url}")
    private String appUrl;

    @Transactional
    public void createAndSend(User user) {
        // Si ya tiene token (reenvío), lo borramos
        tokenRepository.deleteByUser_IdUser(user.getIdUser());

        ActivationToken token = ActivationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();
        tokenRepository.save(token);

        String activationUrl = appUrl + "/auth/activate?token=" + token.getToken();
        emailService.sendActivation(user.getEmail(), user.getUsername(), activationUrl);
    }

    @Transactional
    public ActivationToken validate(String tokenValue) {
        ActivationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Token de activación no válido"));

        if (token.isUsed())
            throw new IllegalStateException("Este enlace ya ha sido utilizado");
        if (token.isExpired())
            throw new IllegalStateException("El enlace de activación ha expirado");

        return token;
    }

    public void sendWelcomeIfVerified(User user) {
        emailService.sendWelcome(user.getEmail(), user.getUsername());
    }

    @Transactional
    public void markUsed(ActivationToken token) {
        token.setUsed(true);
        token.getUser().setEnabled(true);
        tokenRepository.save(token);
    }
}
