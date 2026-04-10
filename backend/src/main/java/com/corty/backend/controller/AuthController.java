package com.corty.backend.controller;

import com.corty.backend.dto.*;
import com.corty.backend.model.ActivationToken;
import com.corty.backend.model.City;
import com.corty.backend.model.Player;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.services.ActivationService;
import com.corty.backend.services.AuthService;
import com.corty.backend.services.EmailService;
import com.corty.backend.services.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ActivationService activationService;
    private final JwtService jwtService;
    private final PlayerRepository playerRepository;
    private final CityRepository cityRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterPlayerRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** Valida el token y devuelve los datos básicos para pre-rellenar el formulario */
    @GetMapping("/activate")
    public ResponseEntity<ActivationInfoResponse> getActivationInfo(@RequestParam String token) {
        ActivationToken activationToken = activationService.validate(token);
        User user = activationToken.getUser();
        return ResponseEntity.ok(ActivationInfoResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .build());
    }

    /** Completa el perfil y activa la cuenta */
    @PostMapping("/activate")
    public ResponseEntity<AuthResponse> completeProfile(@Valid @RequestBody CompleteProfileRequest request) {
        ActivationToken activationToken = activationService.validate(request.getToken());
        User user = activationToken.getUser();

        // Completar el Player asociado
        Player player = playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new RuntimeException("Perfil de jugador no encontrado"));

        player.setName(request.getName());
        player.setSurname(request.getSurname());
        player.setPhone(request.getPhone());
        player.setGender(request.getGender());
        player.setBirthDate(request.getBirthDate());
        player.setBiography(request.getBiography());

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId()).orElse(null);
            player.setCity(city);
        }

        playerRepository.save(player);

        // Activar cuenta y marcar token como usado
        activationService.markUsed(activationToken);

        // Enviar email de bienvenida
        activationService.sendWelcomeIfVerified(user);

        // Devolver JWT para que el usuario quede logueado directamente
        String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder().token(jwt).build());
    }
}
