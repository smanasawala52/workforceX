package com.workforcex.backend.controller;

import com.workforcex.backend.dto.UserResponse;
import com.workforcex.backend.service.CSRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/csr")
@RequiredArgsConstructor
public class CSRController {

    private final CSRService csrService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(csrService.getAllUsers());
    }
}
