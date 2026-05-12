package com.corty.backend.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.corty.backend.dto.PlayerAdminCreateRequest;
import com.corty.backend.dto.PlayerAdminRequest;
import com.corty.backend.dto.PlayerAdminResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.PlayerMapper;
import com.corty.backend.model.City;
import com.corty.backend.model.Player;
import com.corty.backend.model.Role;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.Gender;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.RoleRepository;
import com.corty.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerAdminService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final RoleRepository roleRepository;
    private final PlayerMapper playerMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;

    @Transactional
    public PlayerAdminResponse create(PlayerAdminCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EntityInUseException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityInUseException("El email ya está registrado");
        }

        Role role = roleRepository.findByName("PLAYER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol PLAYER no encontrado"));

        boolean verified = Boolean.TRUE.equals(request.getVerified());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(verified)
                .build();
        userRepository.save(user);

        City city = request.getCityId() != null
                ? cityRepository.findById(request.getCityId()).orElse(null)
                : null;

        boolean hasFullProfile = request.getName() != null && !request.getName().isBlank();
        Player player = Player.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : null)
                .birthDate(request.getBirthDate())
                .profileComplete(hasFullProfile)
                .biography(request.getBiography())
                .avatarUrl(request.getAvatarUrl())
                .city(city)
                .user(user)
                .build();
        playerRepository.save(player);

        if (!verified) {
            activationService.createAndSend(user);
        } else {
            activationService.sendWelcomeIfVerified(user);
        }

        return playerMapper.toAdminResponse(player);
    }

    public List<PlayerAdminResponse> createBatch(MultipartFile file) {
        Role playerRole = roleRepository.findByName("PLAYER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol PLAYER no encontrado"));

        List<PlayerAdminResponse> createdPlayers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            List<String> dataLines = reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());

            for (int index = 0; index < dataLines.size(); index++) {
                String[] columns = dataLines.get(index).split(",", -1);
                String email = columns[0].trim();
                String baseUsername = columns.length > 1 && !columns[1].isBlank() ? columns[1].trim() : email.split("@")[0];
                String password = columns.length > 2 && !columns[2].isBlank() ? columns[2].trim() : UUID.randomUUID().toString();
                String firstName = columns.length > 3 ? columns[3].trim() : "";
                String lastName = columns.length > 4 ? columns[4].trim() : "";
                String phone = columns.length > 5 ? columns[5].trim() : "";
                String gender = columns.length > 6 && !columns[6].isBlank() ? columns[6].trim().toUpperCase() : "OTHER";
                String birthDate = columns.length > 7 && !columns[7].isBlank() ? columns[7].trim() : "2000-01-01";
                boolean isVerified = columns.length > 8 && "true".equalsIgnoreCase(columns[8].trim());

                boolean alreadyExists = userRepository.existsByEmail(email);
                if (alreadyExists) {
                    log.warn("CSV línea {}: email '{}' ya existe, se omite", index + 2, email);
                } else {
                    try {
                        String username = resolveUniqueUsername(baseUsername);
                        PlayerAdminResponse created = createSingleFromBatch(
                                playerRole, email, username, password,
                                firstName, lastName, phone, gender, birthDate, isVerified);
                        createdPlayers.add(created);
                    } catch (DataIntegrityViolationException exception) {
                        log.warn("CSV línea {}: conflicto de datos para '{}', se omite", index + 2, email);
                    }
                }
            }
        } catch (EntityInUseException entityInUseException) {
            throw entityInUseException;
        } catch (Exception exception) {
            throw new EntityInUseException("Error procesando CSV: " + exception.getMessage());
        }
        return createdPlayers;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PlayerAdminResponse createSingleFromBatch(Role playerRole, String email, String username,
            String password, String firstName, String lastName, String phone, String gender,
            String birthDate, boolean isVerified) {
        User newUser = userRepository.save(User.builder()
                .username(username).email(email)
                .password(passwordEncoder.encode(password))
                .role(playerRole).enabled(isVerified)
                .build());

        boolean hasFullProfile = !firstName.isBlank();
        Player savedPlayer = playerRepository.save(Player.builder()
                .name(firstName.isBlank() ? null : firstName)
                .surname(lastName.isBlank() ? null : lastName)
                .phone(phone.isBlank() ? null : phone)
                .gender(gender.isBlank() ? null : Gender.valueOf(gender))
                .birthDate(birthDate.isBlank() ? null : LocalDate.parse(birthDate))
                .profileComplete(hasFullProfile)
                .user(newUser)
                .build());

        if (!isVerified) {
            activationService.createAndSend(newUser);
        } else {
            activationService.sendWelcomeIfVerified(newUser);
        }
        return playerMapper.toAdminResponse(savedPlayer);
    }

    public List<PlayerAdminResponse> getAll() {
        return playerMapper.toAdminResponseList(playerRepository.findAll());
    }

    public PlayerAdminResponse getById(Long id) {
        return playerMapper.toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public PlayerAdminResponse update(Long id, PlayerAdminRequest request) {
        Player player = findOrThrow(id);
        player.setName(request.getName());
        player.setSurname(request.getSurname());
        player.setPhone(request.getPhone());
        player.setGender(Gender.valueOf(request.getGender()));
        player.setBirthDate(request.getBirthDate());
        player.setBiography(request.getBiography());
        player.setAvatarUrl(request.getAvatarUrl());
        if (request.getKarma() != null) {
            player.setKarma(request.getKarma());
        }
        if (request.getCityId() != null) {
            player.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada")));
        } else {
            player.setCity(null);
        }
        return playerMapper.toAdminResponse(playerRepository.save(player));
    }

    private String resolveUniqueUsername(String base) {
        String candidate = base;
        int suffix = 2;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private Player findOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
    }
}
