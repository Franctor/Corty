package com.corty.backend.controller;

import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.services.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // lat y lon son opcionales — si no se envían se usa el fallback sin distancia
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyCourtResponse>> getNearbyCourts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String sport
    ) {
        return ResponseEntity.ok(courtService.getNearbyCourts(lat, lon, sport));
    }
}
