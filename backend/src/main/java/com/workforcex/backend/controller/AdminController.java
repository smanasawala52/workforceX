package com.workforcex.backend.controller;

import com.workforcex.backend.dto.UserResponse;
import com.workforcex.backend.dto.VerificationResponse;
import com.workforcex.backend.service.AdminService;
import com.workforcex.backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final VerificationService verificationService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/verifications")
    public ResponseEntity<List<VerificationResponse>> getPendingVerifications() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    @PutMapping("/verifications/{id}/approve")
    public ResponseEntity<Void> approveVerification(@PathVariable UUID id) {
        verificationService.approveVerification(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/verifications/{id}/reject")
    public ResponseEntity<Void> rejectVerification(@PathVariable UUID id) {
        verificationService.rejectVerification(id);
        return ResponseEntity.ok().build();
    }
}
