package com.corty.backend.controller;

import com.corty.backend.dto.CityResponse;
import com.corty.backend.dto.ProvinceResponse;
import com.corty.backend.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Returns all provinces ordered alphabetically
    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getAllProvinces() {
        return ResponseEntity.ok(locationService.getAllProvinces());
    }

    // Returns all cities belonging to a province
    @GetMapping("/provinces/{provinceCode}/cities")
    public ResponseEntity<List<CityResponse>> getCitiesByProvince(@PathVariable String provinceCode) {
        return ResponseEntity.ok(locationService.getCitiesByProvince(provinceCode));
    }
}