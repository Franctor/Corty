package com.corty.backend.controller;

import com.corty.backend.dto.PlayerAdminCreateRequest;
import com.corty.backend.dto.PlayerAdminRequest;
import com.corty.backend.dto.PlayerAdminResponse;
import com.corty.backend.dto.PlayerProfileResponse;
import com.corty.backend.dto.PlayerProfileUpdateRequest;
import com.corty.backend.dto.PlayerStatsResponse;
import com.corty.backend.mapper.FriendshipMapper;
import com.corty.backend.mapper.PlayerMapper;
import com.corty.backend.model.User;
import com.corty.backend.services.PlayerAdminService;
import com.corty.backend.services.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/players")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerMapper playerMapper;
    private final FriendshipMapper friendshipMapper;
    private final PlayerAdminService playerAdminService;

    @GetMapping("/me")
    public ResponseEntity<PlayerProfileResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.getMyProfile(currentUser.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<PlayerProfileResponse> updateMyProfile(
            @Valid @RequestBody PlayerProfileUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.updateMyProfile(currentUser.getUsername(), request));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<PlayerStatsResponse> getMyStats(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.getMyStats(currentUser.getUsername()));
    }

    @GetMapping("/search")
    public ResponseEntity<PlayerProfileResponse> searchByUsername(
            @RequestParam String username,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.getPlayerByUsername(username, currentUser.getUsername()));
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<List<PlayerProfileResponse>> suggest(
            @RequestParam String q,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.searchPlayers(q, currentUser.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerProfileResponse> getPlayerProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(playerService.getPlayerProfile(id, currentUser.getUsername()));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<PlayerAdminResponse> create(@Valid @RequestBody PlayerAdminCreateRequest request) {
        return ResponseEntity.status(201).body(playerAdminService.create(request));
    }

    @PostMapping("/admin/csv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<List<PlayerAdminResponse>> createBatch(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(playerAdminService.createBatch(file));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<List<PlayerAdminResponse>> getAll() {
        return ResponseEntity.ok(playerAdminService.getAll());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<PlayerAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerAdminService.getById(id));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<PlayerAdminResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerAdminRequest request) {
        return ResponseEntity.ok(playerAdminService.update(id, request));
    }
}
