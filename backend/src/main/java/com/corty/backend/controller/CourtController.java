package com.corty.backend.controller;

import com.corty.backend.dto.CourtAdminResponse;
import com.corty.backend.dto.CourtDetailResponse;
import com.corty.backend.dto.CourtRequest;
import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.services.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.corty.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Courts")
@RestController
@RequestMapping("api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyCourtResponse>> getNearbyCourts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String surface,
            @RequestParam(required = false) Boolean covered,
            @RequestParam(required = false) Boolean lighting,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "distance") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Double radiusKm
    ) {
        return ResponseEntity.ok(courtService.getNearbyCourts(lat, lon, sport, surface, covered, lighting, maxPrice, sortBy, sortDir, radiusKm));
    }

    @GetMapping
    public ResponseEntity<Page<CourtAdminResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(courtService.getAllAdmin(page, size, search, sort, dir, principal));
    }

    @GetMapping("/by-club/{clubId}")
    public ResponseEntity<List<CourtAdminResponse>> getByClub(@PathVariable Long clubId, @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(courtService.getByClub(clubId, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(courtService.getByIdForUser(id, principal));
    }

    @PostMapping
    public ResponseEntity<CourtAdminResponse> create(@Valid @RequestBody CourtRequest request) {
        return ResponseEntity.status(201).body(courtService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourtAdminResponse> update(@PathVariable Long id, @Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courtService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAuthority('FORCE_DELETE')")
    public ResponseEntity<Void> forceDelete(@PathVariable Long id) {
        courtService.forceDelete(id);
        return ResponseEntity.noContent().build();
    }
}
