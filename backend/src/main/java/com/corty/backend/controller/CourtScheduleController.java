package com.corty.backend.controller;

import com.corty.backend.dto.CourtBlockRequest;
import com.corty.backend.dto.CourtBlockResponse;
import com.corty.backend.dto.CourtScheduleRequest;
import com.corty.backend.dto.CourtScheduleResponse;
import com.corty.backend.services.CourtScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/courts/{courtId}/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'ORGANIZATION')")
public class CourtScheduleController {

    private final CourtScheduleService courtScheduleService;

    @GetMapping
    public ResponseEntity<List<CourtScheduleResponse>> getSchedules(@PathVariable Long courtId) {
        return ResponseEntity.ok(courtScheduleService.getSchedules(courtId));
    }

    @PutMapping
    public ResponseEntity<List<CourtScheduleResponse>> replaceSchedules(
            @PathVariable Long courtId,
            @Valid @RequestBody List<CourtScheduleRequest> requests
    ) {
        return ResponseEntity.ok(courtScheduleService.replaceSchedules(courtId, requests));
    }

    @PatchMapping("/use-club-schedule")
    public ResponseEntity<Void> updateUseClubSchedule(
            @PathVariable Long courtId,
            @RequestBody Map<String, Boolean> body
    ) {
        courtScheduleService.updateUseClubSchedule(courtId, Boolean.TRUE.equals(body.get("useClubSchedule")));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/slot-duration")
    public ResponseEntity<Void> updateSlotDuration(
            @PathVariable Long courtId,
            @RequestBody Map<String, Integer> body
    ) {
        courtScheduleService.updateSlotDuration(courtId, body.get("slotDurationMinutes"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocks")
    public ResponseEntity<List<CourtBlockResponse>> getBlocks(@PathVariable Long courtId) {
        return ResponseEntity.ok(courtScheduleService.getBlocks(courtId));
    }

    @PostMapping("/blocks")
    public ResponseEntity<CourtBlockResponse> addBlock(
            @PathVariable Long courtId,
            @Valid @RequestBody CourtBlockRequest request
    ) {
        return ResponseEntity.status(201).body(courtScheduleService.addBlock(courtId, request));
    }

    @DeleteMapping("/blocks/{blockId}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long courtId, @PathVariable Long blockId) {
        courtScheduleService.deleteBlock(courtId, blockId);
        return ResponseEntity.noContent().build();
    }
}
