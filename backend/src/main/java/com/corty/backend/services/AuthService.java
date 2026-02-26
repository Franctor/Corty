package com.corty.backend.services;

import com.corty.backend.dto.AuthResponse;
import com.corty.backend.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    public AuthResponse register(RegisterRequest request) {
        return null;
    }
}
