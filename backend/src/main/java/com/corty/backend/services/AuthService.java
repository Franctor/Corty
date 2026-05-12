package com.corty.backend.services;

import java.time.LocalDate;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.corty.backend.dto.ActivationInfoResponse;
import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.CompleteProfileRequest;
import com.corty.backend.dto.LoginRequest;
import com.corty.backend.dto.RegisterPlayerRequest;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.exception.ServerConfigurationException;
import com.corty.backend.exception.UserAlreadyExistsException;
import com.corty.backend.model.ActivationToken;
import com.corty.backend.model.City;
import com.corty.backend.model.Organization;
import com.corty.backend.model.Player;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.OrganizationRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerRepository playerRepository;
    private final ActivationService activationService;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterPlayerRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        Role playerRole = roleRepository.findByName("PLAYER")
                .orElseThrow(() -> new ServerConfigurationException("Rol PLAYER no encontrado"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(playerRole)
                .enabled(false)
                .build();

        userRepository.save(user);

        Player player = Player.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .biography(request.getBiography())
                .avatarUrl(request.getAvatarUrl())
                .city(city)
                .profileComplete(true)
                .user(user)
                .build();

        playerRepository.save(player);
        activationService.createAndSend(user);

        return AuthResponse.builder().build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Transactional(readOnly = true)
    public ActivationInfoResponse getActivationInfo(String token) {
        ActivationToken activationToken = activationService.validate(token);
        User user = activationToken.getUser();

        Player player = playerRepository.findByUser_IdUser(user.getIdUser()).orElse(null);
        Organization organization = organizationRepository.findByUser_IdUser(user.getIdUser()).orElse(null);

        boolean isOrg = organization != null;
        boolean needsProfile = isOrg
                ? (organization.getBusinessName() == null || organization.getCif() == null)
                : (player != null && !player.isProfileComplete());
        String userType = isOrg ? "ORGANIZATION" : "PLAYER";

        return ActivationInfoResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .needsProfile(needsProfile)
                .userType(userType)
                .build();
    }

    @Transactional
    public AuthResponse activateAccount(CompleteProfileRequest request) {
        ActivationToken activationToken = activationService.validate(request.getToken());
        User user = activationToken.getUser();

        Player player = playerRepository.findByUser_IdUser(user.getIdUser()).orElse(null);
        Organization organization = organizationRepository.findByUser_IdUser(user.getIdUser()).orElse(null);

        updateUsernameIfProvided(user, request.getUsername());

        if (organization != null && (organization.getBusinessName() == null || organization.getCif() == null)) {
            validateOrgProfileFields(request);
            completeOrganizationProfile(organization, request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else if (player != null && !player.isProfileComplete()) {
            validateProfileFields(request);
            completePlayerProfile(player, request);
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        user.setEnabled(true);
        userRepository.save(user);

        activationService.markUsed(activationToken);
        activationService.sendWelcomeIfVerified(user);

        String jwt = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwt).build();
    }

    @Transactional
    public void deleteOwnAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        userService.delete(user.getIdUser());
    }

    private void updateUsernameIfProvided(User user, String newUsername) {
        if (newUsername != null && !newUsername.isBlank()) {
            String trimmedUsername = newUsername.trim();
            boolean usernameTaken = !trimmedUsername.equals(user.getUsername())
                    && userRepository.existsByUsername(trimmedUsername);
            if (usernameTaken) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso");
            }
            user.setUsername(trimmedUsername);
        }
    }

    private void completeOrganizationProfile(Organization organization, CompleteProfileRequest request) {
        organization.setBusinessName(request.getBusinessName().trim());
        organization.setCif(request.getCif().trim());
        if (request.getFiscalCityId() != null) {
            organization.setFiscalCity(cityRepository.findById(request.getFiscalCityId()).orElse(null));
        }
        organizationRepository.save(organization);
    }

    private void completePlayerProfile(Player player, CompleteProfileRequest request) {
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
    }

    private void validateOrgProfileFields(CompleteProfileRequest request) {
        if (request.getBusinessName() == null || request.getBusinessName().isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (request.getCif() == null || request.getCif().isBlank()) {
            throw new IllegalArgumentException("El CIF es obligatorio");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }

    private void validateProfileFields(CompleteProfileRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (request.getSurname() == null || request.getSurname().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (request.getGender() == null) {
            throw new IllegalArgumentException("El género es obligatorio");
        }
        if (request.getBirthDate() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (request.getBirthDate().isAfter(LocalDate.now().minusYears(16))) {
            throw new IllegalArgumentException("Debes tener al menos 16 años");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }
}
