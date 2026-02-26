package com.corty.backend.controller;

import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.RegisterRequest;
import com.corty.backend.mapper.AuthMapper;
import com.corty.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthMapper authMapper;
    private final AuthService authService;

    @PostMapping("/login")
    public void a(){

    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
