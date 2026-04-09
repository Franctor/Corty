package com.corty.backend.controller;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.dto.SportRequest;
import com.corty.backend.dto.SportResponse;
import com.corty.backend.services.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping("/filters")
    public ResponseEntity<List<SportFilterResponse>> getTopSportsForFilter() {
        return ResponseEntity.ok(sportService.getTopSportsForFilter());
    }

    @GetMapping
    public ResponseEntity<List<SportResponse>> getAll() {
        return ResponseEntity.ok(sportService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sportService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SportResponse> create(@Valid @RequestBody SportRequest request) {
        return ResponseEntity.ok(sportService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SportResponse> update(@PathVariable Long id, @Valid @RequestBody SportRequest request) {
        return ResponseEntity.ok(sportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
