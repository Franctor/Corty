package com.corty.backend.services;

import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.LoginRequest;
import com.corty.backend.dto.RegisterPlayerRequest;
import com.corty.backend.exception.CortyException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.exception.ServerConfigurationException;
import com.corty.backend.exception.UserAlreadyExistsException;
import com.corty.backend.model.City;
import com.corty.backend.model.Player;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Transactional
    public AuthResponse register(RegisterPlayerRequest request) {

        // Check for duplicate username or email
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

        // Create and persist the user account
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(playerRole)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Create and persist the player profile linked to the user
        Player player = Player.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .biography(request.getBiography())
                .avatarUrl(request.getAvatarUrl())
                .city(city)
                .user(user)
                .build();

        playerRepository.save(player);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
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
}
