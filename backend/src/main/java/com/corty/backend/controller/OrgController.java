package com.corty.backend.controller;

import com.corty.backend.dto.OrgAdminCreateRequest;
import com.corty.backend.dto.OrgAdminRequest;
import com.corty.backend.dto.OrgAdminResponse;
import com.corty.backend.services.OrgAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/organizations")
@RequiredArgsConstructor
public class OrgController {

    private final OrgAdminService orgAdminService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<OrgAdminResponse> create(@Valid @RequestBody OrgAdminCreateRequest request) {
        return ResponseEntity.status(201).body(orgAdminService.create(request));
    }

    @PostMapping("/csv")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<List<OrgAdminResponse>> createBatch(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(orgAdminService.createBatch(file));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<List<OrgAdminResponse>> getAll() {
        return ResponseEntity.ok(orgAdminService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<OrgAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orgAdminService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<OrgAdminResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrgAdminRequest request) {
        return ResponseEntity.ok(orgAdminService.update(id, request));
    }
}
