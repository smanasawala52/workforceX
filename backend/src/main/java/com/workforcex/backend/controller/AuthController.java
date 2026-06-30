package com.workforcex.backend.controller;

import com.workforcex.backend.dto.LoginRequest;
import com.workforcex.backend.dto.LoginResponse;
import com.workforcex.backend.dto.RegisterRequest;
import com.workforcex.backend.dto.RegisterResponse;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.security.JwtUtil;
import com.workforcex.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.fromEntity(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = jwtUtil.generateToken(user.getMobileNumber(), user.getRole().name());
        return ResponseEntity.ok(LoginResponse.fromEntity(user, token));
    }
}
