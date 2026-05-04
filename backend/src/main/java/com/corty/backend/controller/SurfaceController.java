package com.corty.backend.controller;

import com.corty.backend.dto.SurfaceRequest;
import com.corty.backend.dto.SurfaceResponse;
import com.corty.backend.services.SurfaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/surfaces")
@RequiredArgsConstructor
public class SurfaceController {

    private final SurfaceService surfaceService;

    @GetMapping
    public ResponseEntity<List<SurfaceResponse>> getAll() {
        return ResponseEntity.ok(surfaceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurfaceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(surfaceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SurfaceResponse> create(@Valid @RequestBody SurfaceRequest request) {
        return ResponseEntity.status(201).body(surfaceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurfaceResponse> update(@PathVariable Long id, @Valid @RequestBody SurfaceRequest request) {
        return ResponseEntity.ok(surfaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surfaceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
