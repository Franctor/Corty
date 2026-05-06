package com.corty.backend.service;

import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.LoginRequest;
import com.corty.backend.dto.RegisterPlayerRequest;
import com.corty.backend.exception.UserAlreadyExistsException;
import com.corty.backend.model.*;
import com.corty.backend.model.enums.Gender;
import com.corty.backend.repository.*;
import com.corty.backend.services.ActivationService;
import com.corty.backend.services.AuthService;
import com.corty.backend.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — registro y login")
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock RoleRepository roleRepository;
    @Mock CityRepository cityRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PlayerRepository playerRepository;
    @Mock ActivationService activationService;
    @Mock OrganizationRepository organizationRepository;

    @InjectMocks AuthService authService;

    private RegisterPlayerRequest validRequest;
    private Role playerRole;
    private City city;

    @BeforeEach
    void setUp() {
        playerRole = Role.builder().idRole(1L).name("PLAYER").build();
        city = City.builder().idCity(1L).label("Madrid").build();

        validRequest = RegisterPlayerRequest.builder()
                .username("jugador1")
                .email("jugador@test.com")
                .password("Passw0rd!")
                .name("Juan")
                .surname("García")
                .phone("612345678")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1995, 5, 20))
                .cityId(1L)
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-A01: Registro con datos válidos → usuario y jugador creados, activación enviada")
    void register_valid_request_creates_user_and_player() {
        when(userRepository.existsByUsername("jugador1")).thenReturn(false);
        when(userRepository.existsByEmail("jugador@test.com")).thenReturn(false);
        when(roleRepository.findByName("PLAYER")).thenReturn(Optional.of(playerRole));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(passwordEncoder.encode(any())).thenReturn("hashedpassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(validRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(argThat(u ->
                "jugador1".equals(u.getUsername()) && !u.isEnabled()
        ));
        verify(playerRepository).save(argThat(p ->
                "Juan".equals(p.getName()) && "García".equals(p.getSurname())
        ));
        verify(activationService).createAndSend(any());
    }

    @Test
    @DisplayName("TC-A02: Username duplicado → UserAlreadyExistsException")
    void register_duplicate_username_throws() {
        when(userRepository.existsByUsername("jugador1")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("usuario");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-A03: Email duplicado → UserAlreadyExistsException")
    void register_duplicate_email_throws() {
        when(userRepository.existsByUsername("jugador1")).thenReturn(false);
        when(userRepository.existsByEmail("jugador@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-A04: Ciudad no encontrada → ResourceNotFoundException")
    void register_city_not_found_throws() {
        when(userRepository.existsByUsername("jugador1")).thenReturn(false);
        when(userRepository.existsByEmail("jugador@test.com")).thenReturn(false);
        when(roleRepository.findByName("PLAYER")).thenReturn(Optional.of(playerRole));
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(com.corty.backend.exception.ResourceNotFoundException.class);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-A05: Login con credenciales válidas → devuelve JWT")
    void login_valid_credentials_returns_token() {
        User user = User.builder()
                .idUser(1L)
                .username("jugador1")
                .email("jugador@test.com")
                .role(playerRole)
                .enabled(true)
                .build();

        LoginRequest request = LoginRequest.builder()
                .username("jugador1")
                .password("Passw0rd!")
                .build();

        when(userRepository.findByUsername("jugador1")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token-123");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("jugador1", "Passw0rd!")
        );
    }

    @Test
    @DisplayName("TC-A06: Login con contraseña incorrecta → BadCredentialsException")
    void login_wrong_password_throws() {
        LoginRequest request = LoginRequest.builder()
                .username("jugador1")
                .password("wrong")
                .build();

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
