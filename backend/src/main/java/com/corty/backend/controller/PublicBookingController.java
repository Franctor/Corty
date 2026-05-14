package com.corty.backend.controller;

import com.corty.backend.dto.JoinRequestResponse;
import com.corty.backend.dto.JoinPaymentCheckResponse;
import com.corty.backend.dto.PublicBookingResponse;
import com.corty.backend.model.User;
import com.corty.backend.services.PublicBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Bookings")
@RestController
@RequestMapping("api/bookings/public")
@RequiredArgsConstructor
public class PublicBookingController {

    private final PublicBookingService publicBookingService;

    @GetMapping
    public ResponseEntity<List<PublicBookingResponse>> getPublicBookings(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "50") double radiusKm,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Double levelMin,
            @RequestParam(required = false) Double levelMax,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                publicBookingService.findPublicBookings(lat, lon, radiusKm, sport, dateFrom, dateTo, levelMin, levelMax, limit, currentUser.getUsername())
        );
    }

    @GetMapping("/{bookingId}/join/payment-check")
    public ResponseEntity<JoinPaymentCheckResponse> joinPaymentCheck(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(publicBookingService.joinPaymentCheck(bookingId, currentUser.getUsername()));
    }

    @PostMapping("/{bookingId}/join")
    public ResponseEntity<Void> sendJoinRequest(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        publicBookingService.sendJoinRequest(bookingId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookingId}/join")
    public ResponseEntity<Void> cancelJoinRequest(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        publicBookingService.cancelJoinRequest(bookingId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/requests")
    public ResponseEntity<List<JoinRequestResponse>> getPendingRequests(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                publicBookingService.getPendingRequests(bookingId, currentUser.getUsername())
        );
    }

    @PostMapping("/{bookingId}/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @PathVariable Long bookingId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) {
        publicBookingService.acceptJoinRequest(bookingId, requestId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable Long bookingId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) {
        publicBookingService.rejectJoinRequest(bookingId, requestId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
