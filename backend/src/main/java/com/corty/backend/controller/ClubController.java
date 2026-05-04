package com.corty.backend.controller;

import com.corty.backend.dto.ClubRequest;
import com.corty.backend.dto.ClubResponse;
import com.corty.backend.dto.ClubStatsResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public ResponseEntity<List<ClubResponse>> getAll() {
        return ResponseEntity.ok(clubService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClubResponse> create(
            @Valid @RequestBody ClubRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(201).body(clubService.create(request, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClubRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(clubService.update(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAuthority('FORCE_DELETE')")
    public ResponseEntity<Void> forceDelete(@PathVariable Long id) {
        clubService.forceDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-org/{orgId}")
    public ResponseEntity<List<ClubResponse>> getByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(clubService.getByOrganizationId(orgId));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ClubStatsResponse> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getStats(id));
    }
}
