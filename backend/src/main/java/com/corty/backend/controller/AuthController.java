package com.corty.backend.controller;

import com.corty.backend.dto.*;
import com.corty.backend.model.ActivationToken;
import com.corty.backend.model.City;
import com.corty.backend.model.Organization;
import com.corty.backend.model.Player;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.UserRepository;
import com.corty.backend.services.ActivationService;
import com.corty.backend.services.AuthService;
import com.corty.backend.services.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ActivationService activationService;
    private final JwtService jwtService;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        ActivationToken activationToken = activationService.validate(token);
        User user = activationToken.getUser();
        Player player = playerRepository.findByUser_IdUser(user.getIdUser()).orElse(null);
        Organization organization = organizationRepository.findByUser_IdUser(user.getIdUser()).orElse(null);

        boolean isOrg = organization != null;
        boolean needsProfile = isOrg
                ? (organization.getBusinessName() == null || organization.getCif() == null)
                : (player != null && !player.isProfileComplete());
        String userType = isOrg ? "ORGANIZATION" : "PLAYER";

        return ResponseEntity.ok(ActivationInfoResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .needsProfile(needsProfile)
                .userType(userType)
                .build());
    }

    /** Activa la cuenta. Si needsProfile era true, valida y guarda los datos del perfil. */
    @PostMapping("/activate")
    public ResponseEntity<AuthResponse> activateAccount(@RequestBody CompleteProfileRequest request) {
        ActivationToken activationToken = activationService.validate(request.getToken());
        User user = activationToken.getUser();

        Player player = playerRepository.findByUser_IdUser(user.getIdUser()).orElse(null);
        Organization organization = organizationRepository.findByUser_IdUser(user.getIdUser()).orElse(null);

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            boolean usernameTaken = !newUsername.equals(user.getUsername())
                    && userRepository.existsByUsername(newUsername);
            if (usernameTaken) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso");
            }
            user.setUsername(newUsername);
        }

        if (organization != null && (organization.getBusinessName() == null || organization.getCif() == null)) {
            validateOrgProfileFields(request);
            organization.setBusinessName(request.getBusinessName().trim());
            organization.setCif(request.getCif().trim());
            if (request.getFiscalCityId() != null) {
                organization.setFiscalCity(cityRepository.findById(request.getFiscalCityId()).orElse(null));
            }
            organizationRepository.save(organization);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else if (player != null && !player.isProfileComplete()) {
            validateProfileFields(request);
            player.setName(request.getName().trim());
            player.setSurname(request.getSurname().trim());
            player.setPhone(request.getPhone().trim());
            player.setGender(request.getGender());
            player.setBirthDate(request.getBirthDate());
            player.setBiography(request.getBiography());
            player.setProfileComplete(true);
            if (request.getCityId() != null) {
                player.setCity(cityRepository.findById(request.getCityId()).orElse(null));
            }
            playerRepository.save(player);
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        user.setEnabled(true);
        userRepository.save(user);
        activationService.markUsed(activationToken);
        activationService.sendWelcomeIfVerified(user);

        String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder().token(jwt).build());
    }

    private void validateOrgProfileFields(CompleteProfileRequest request) {
        if (request.getBusinessName() == null || request.getBusinessName().isBlank())
            throw new IllegalArgumentException("La razón social es obligatoria");
        if (request.getCif() == null || request.getCif().isBlank())
            throw new IllegalArgumentException("El CIF es obligatorio");
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
    }

    private void validateProfileFields(CompleteProfileRequest request) {
        if (request.getName() == null || request.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (request.getSurname() == null || request.getSurname().isBlank())
            throw new IllegalArgumentException("El apellido es obligatorio");
        if (request.getPhone() == null || request.getPhone().isBlank())
            throw new IllegalArgumentException("El teléfono es obligatorio");
        if (request.getGender() == null)
            throw new IllegalArgumentException("El género es obligatorio");
        if (request.getBirthDate() == null)
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        if (request.getBirthDate().isAfter(LocalDate.now().minusYears(16)))
            throw new IllegalArgumentException("Debes tener al menos 16 años");
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
    }
}
