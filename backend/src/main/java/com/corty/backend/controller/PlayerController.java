package com.corty.backend.controller;

import com.corty.backend.dto.PlayerAdminCreateRequest;
import com.corty.backend.dto.PlayerAdminRequest;
import com.corty.backend.dto.PlayerAdminResponse;
import com.corty.backend.mapper.FriendshipMapper;
import com.corty.backend.mapper.PlayerMapper;
import com.corty.backend.services.PlayerAdminService;
import com.corty.backend.services.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<PlayerAdminResponse> create(@Valid @RequestBody PlayerAdminCreateRequest request) {
        return ResponseEntity.status(201).body(playerAdminService.create(request));
    }

    @PostMapping("/admin/csv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN') or hasAuthority('MANAGE_STAFF')")
    public ResponseEntity<List<PlayerAdminResponse>> createBatch(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(playerAdminService.createBatch(file));
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
