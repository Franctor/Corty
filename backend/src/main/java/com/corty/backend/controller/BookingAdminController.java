package com.corty.backend.controller;

import com.corty.backend.dto.BookingAdminDetailResponse;
import com.corty.backend.dto.BookingAdminResponse;
import com.corty.backend.dto.BookingAdminUpdateRequest;
import com.corty.backend.dto.BookingPresencialRequest;
import com.corty.backend.services.BookingAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.corty.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'ORGANIZATION')")
public class BookingAdminController {

    private final BookingAdminService bookingAdminService;

    @GetMapping
    public ResponseEntity<Page<BookingAdminResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(bookingAdminService.getAll(page, size, search, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingAdminDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingAdminService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BookingAdminDetailResponse> createPresencial(
            @Valid @RequestBody BookingPresencialRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(201).body(bookingAdminService.createPresencial(request, principal));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingAdminDetailResponse> update(
            @PathVariable Long id,
            @RequestBody BookingAdminUpdateRequest request) {
        return ResponseEntity.ok(bookingAdminService.update(id, request));
    }
}
