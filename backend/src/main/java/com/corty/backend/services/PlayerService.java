package com.corty.backend.services;

import com.corty.backend.dto.PlayerProfileResponse;
import com.corty.backend.dto.PlayerProfileUpdateRequest;
import com.corty.backend.dto.PlayerStatsResponse;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.City;
import com.corty.backend.model.Player;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.PlayerSport;
import com.corty.backend.model.Sport;
import com.corty.backend.model.User;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final PlayerBookingRepository playerBookingRepository;

    @Transactional(readOnly = true)
    public PlayerProfileResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Player player = playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));
        return toResponse(player, user);
    }

    @Transactional
    public PlayerProfileResponse updateMyProfile(String username, PlayerProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Player player = playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado"));

        if (request.getName() != null)      player.setName(request.getName());
        if (request.getSurname() != null)   player.setSurname(request.getSurname());
        if (request.getBiography() != null) player.setBiography(request.getBiography());
        if (request.getAvatarUrl() != null) player.setAvatarUrl(request.getAvatarUrl());
        player.setPublicProfile(request.isPublicProfile());

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada"));
            player.setCity(city);
        }

        playerRepository.save(player);
        return toResponse(player, user);
    }

    @Transactional(readOnly = true)
    public PlayerStatsResponse getMyStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Player player = playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));

        List<PlayerBooking> bookings = playerBookingRepository.findCompletedWithResultByPlayerId(player.getIdPlayer());

        Map<Long, PlayerStatsResponse.SportStat> sportMap = new LinkedHashMap<>();

        for (PlayerBooking pb : bookings) {
            Sport sport = pb.getBooking().getCourt().getSport();
            Long sportId = sport.getIdSport();

            sportMap.computeIfAbsent(sportId, id -> PlayerStatsResponse.SportStat.builder()
                    .sport(sport.getName())
                    .sportIconUrl(sport.getIconUrl())
                    .sportColor(sport.getColor())
                    .matches(0).wins(0).losses(0)
                    .build());

            PlayerStatsResponse.SportStat stat = sportMap.get(sportId);
            stat.setMatches(stat.getMatches() + 1);
            if (Boolean.TRUE.equals(pb.isWinner())) stat.setWins(stat.getWins() + 1);
            else stat.setLosses(stat.getLosses() + 1);
        }

        Map<Long, Double> levelMap = new LinkedHashMap<>();
        for (PlayerSport ps : player.getSportsProfiles()) {
            levelMap.put(ps.getSport().getIdSport(), ps.getLevel());
        }

        sportMap.forEach((sportId, stat) -> stat.setLevel(levelMap.getOrDefault(sportId, 0.0)));

        List<PlayerStatsResponse.SportStat> sports = new ArrayList<>(sportMap.values());
        int total  = sports.stream().mapToInt(PlayerStatsResponse.SportStat::getMatches).sum();
        int wins   = sports.stream().mapToInt(PlayerStatsResponse.SportStat::getWins).sum();
        int losses = sports.stream().mapToInt(PlayerStatsResponse.SportStat::getLosses).sum();

        return PlayerStatsResponse.builder()
                .totalMatches(total)
                .totalWins(wins)
                .totalLosses(losses)
                .karma(player.getKarma())
                .sports(sports)
                .build();
    }

    @Transactional(readOnly = true)
    public PlayerProfileResponse getPlayerByUsername(String username, String viewerUsername) {
        Player player = playerRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        boolean isOwner = player.getUser().getUsername().equals(viewerUsername);
        if (!isOwner && !player.isPublicProfile()) {
            throw new ResourceNotFoundException("Perfil privado");
        }
        return toResponse(player, player.getUser());
    }

    public PlayerProfileResponse getPlayerProfile(Long playerId, String viewerUsername) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));

        boolean isOwner = player.getUser().getUsername().equals(viewerUsername);
        if (!isOwner && !player.isPublicProfile()) {
            throw new ResourceNotFoundException("Perfil privado");
        }

        return toResponse(player, player.getUser());
    }

    private PlayerProfileResponse toResponse(Player player, User user) {
        List<PlayerProfileResponse.SportProfileResponse> sports = player.getSportsProfiles().stream()
                .map(ps -> PlayerProfileResponse.SportProfileResponse.builder()
                        .sportId(ps.getSport().getIdSport())
                        .sport(ps.getSport().getName())
                        .sportIconUrl(ps.getSport().getIconUrl())
                        .sportColor(ps.getSport().getColor())
                        .level(ps.getLevel())
                        .playedMatches(ps.getPlayedMatches())
                        .wins(ps.getWins())
                        .losses(ps.getLosses())
                        .build())
                .toList();

        return PlayerProfileResponse.builder()
                .id(player.getIdPlayer())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(player.getName())
                .surname(player.getSurname())
                .avatarUrl(player.getAvatarUrl())
                .biography(player.getBiography())
                .phone(player.getPhone())
                .gender(player.getGender() != null ? player.getGender().name() : null)
                .birthDate(player.getBirthDate())
                .karma(player.getKarma())
                .city(player.getCity() != null ? player.getCity().getLabel() : null)
                .publicProfile(player.isPublicProfile())
                .sports(sports)
                .build();
    }
}
