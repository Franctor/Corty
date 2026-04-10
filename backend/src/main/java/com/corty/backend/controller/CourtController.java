package com.corty.backend.controller;

import com.corty.backend.dto.CourtAdminResponse;
import com.corty.backend.dto.CourtRequest;
import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.services.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyCourtResponse>> getNearbyCourts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String sport
    ) {
        return ResponseEntity.ok(courtService.getNearbyCourts(lat, lon, sport));
    }

    @GetMapping
    public ResponseEntity<List<CourtAdminResponse>> getAll() {
        return ResponseEntity.ok(courtService.getAllAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourtAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courtService.getByIdAdmin(id));
    }

    @PostMapping
    public ResponseEntity<CourtAdminResponse> create(@Valid @RequestBody CourtRequest request) {
        return ResponseEntity.ok(courtService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_PRICING')")
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
