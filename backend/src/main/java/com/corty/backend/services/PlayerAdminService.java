package com.corty.backend.services;

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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        if (userRepository.existsByUsername(request.getUsername()))
            throw new EntityInUseException("El nombre de usuario ya está en uso");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new EntityInUseException("El email ya está registrado");

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

        Player player = Player.builder()
                .name(request.getName() != null ? request.getName() : "")
                .surname(request.getSurname() != null ? request.getSurname() : "")
                .phone(request.getPhone() != null ? request.getPhone() : "")
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : Gender.OTHER)
                .birthDate(request.getBirthDate() != null ? request.getBirthDate() : LocalDate.of(2000, 1, 1))
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

    @Transactional
    public List<PlayerAdminResponse> createBatch(MultipartFile file) {
        Role role = roleRepository.findByName("PLAYER")
                .orElseThrow(() -> new ResourceNotFoundException("Rol PLAYER no encontrado"));

        List<PlayerAdminResponse> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (first) { first = false; continue; } // skip header
                if (line.isBlank()) continue;

                String[] cols = line.split(",", -1);
                // Mínimo: email (1 col). Máximo: email,username,password,name,surname,phone,gender,birthDate,verified
                String email     = cols[0].trim();
                String username  = cols.length > 1 && !cols[1].isBlank() ? cols[1].trim() : email.split("@")[0];
                String password  = cols.length > 2 && !cols[2].isBlank() ? cols[2].trim() : UUID.randomUUID().toString();
                String name      = cols.length > 3 ? cols[3].trim() : "";
                String surname   = cols.length > 4 ? cols[4].trim() : "";
                String phone     = cols.length > 5 ? cols[5].trim() : "";
                String gender    = cols.length > 6 && !cols[6].isBlank() ? cols[6].trim().toUpperCase() : "OTHER";
                String birthDate = cols.length > 7 && !cols[7].isBlank() ? cols[7].trim() : "2000-01-01";
                boolean verified = cols.length > 8 && "true".equalsIgnoreCase(cols[8].trim());

                if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email))
                    continue;

                User user = User.builder()
                        .username(username).email(email)
                        .password(passwordEncoder.encode(password))
                        .role(role).enabled(verified)
                        .build();
                userRepository.save(user);

                Player player = Player.builder()
                        .name(name).surname(surname).phone(phone)
                        .gender(Gender.valueOf(gender))
                        .birthDate(LocalDate.parse(birthDate))
                        .user(user)
                        .build();
                playerRepository.save(player);

                if (!verified) {
                    activationService.createAndSend(user);
                }

                results.add(playerMapper.toAdminResponse(player));
            }
        } catch (EntityInUseException e) {
            throw e;
        } catch (Exception e) {
            throw new EntityInUseException("Error procesando CSV: " + e.getMessage());
        }
        return results;
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
        if (request.getKarma() != null) player.setKarma(request.getKarma());
        if (request.getCityId() != null) {
            player.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada")));
        } else {
            player.setCity(null);
        }
        return playerMapper.toAdminResponse(playerRepository.save(player));
    }

    private Player findOrThrow(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
    }
}
