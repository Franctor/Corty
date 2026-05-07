package com.corty.backend.controller;

import com.corty.backend.dto.*;
import com.corty.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterPlayerRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** Valida el token e indica si el usuario necesita completar su perfil */
    @GetMapping("/activate")
    public ResponseEntity<ActivationInfoResponse> getActivationInfo(@RequestParam String token) {
        return ResponseEntity.ok(authService.getActivationInfo(token));
    }

    /** Activa la cuenta. Si needsProfile era true, valida y guarda los datos del perfil. */
    @PostMapping("/activate")
    public ResponseEntity<AuthResponse> activateAccount(@RequestBody CompleteProfileRequest request) {
        return ResponseEntity.ok(authService.activateAccount(request));
    }

    /** El propio usuario cierra su cuenta. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteOwnAccount(@AuthenticationPrincipal UserDetails userDetails) {
        authService.deleteOwnAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
