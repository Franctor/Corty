package com.corty.backend.controller;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.services.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
