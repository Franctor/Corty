package com.corty.backend.controller;

import com.corty.backend.dto.BookingCreateRequest;
import com.corty.backend.dto.BookingCreateResponse;
import com.corty.backend.dto.BookingDetailResponse;
import com.corty.backend.dto.CancellationResponse;
import com.corty.backend.dto.NextBookingResponse;
import com.corty.backend.dto.RecentActivityResponse;
import com.corty.backend.dto.SlotResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.BookingService;
import com.corty.backend.services.CourtAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final CourtAvailabilityService courtAvailabilityService;

    @GetMapping("/availability")
    public ResponseEntity<List<SlotResponse>> getAvailability(
            @RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(courtAvailabilityService.getAvailableSlots(courtId, date));
    }

    @PostMapping
    public ResponseEntity<BookingCreateResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.createBooking(request, currentUser.getUsername()));
    }

    // Devuelve la próxima reserva del jugador autenticado, o 204 si no tiene ninguna
    @GetMapping("/next")
    public ResponseEntity<NextBookingResponse> getNextBooking(@AuthenticationPrincipal User currentUser) {
        return bookingService.getNextBooking(currentUser.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Devuelve el detalle de una reserva (solo propietario o participante)
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.getBookingDetail(id, currentUser.getUsername()));
    }

    // Owner cancela la reserva completa
    @DeleteMapping("/{id}")
    public ResponseEntity<CancellationResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, currentUser.getUsername()));
    }

    // Participante abandona la reserva
    @DeleteMapping("/{id}/leave")
    public ResponseEntity<CancellationResponse> leaveBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.leaveBooking(id, currentUser.getUsername()));
    }

    // Devuelve las últimas 5 reservas completadas del jugador autenticado
    @GetMapping("/recent")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivity(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(bookingService.getRecentActivity(currentUser.getUsername()));
    }
}
