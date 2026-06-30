package com.workforcex.backend.controller;

import com.workforcex.backend.dto.EmployerProfileRequest;
import com.workforcex.backend.dto.EmployerProfileResponse;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.service.EmployerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/profile")
@RequiredArgsConstructor
public class EmployerProfileController {

    private final EmployerProfileService employerProfileService;

    @PutMapping
    public ResponseEntity<EmployerProfileResponse> saveOrUpdate(
            Authentication authentication,
            @RequestBody EmployerProfileRequest request
    ) {
        String mobileNumber = authentication.getName();
        EmployerProfile profile = employerProfileService.saveOrUpdate(mobileNumber, request);
        return ResponseEntity.ok(EmployerProfileResponse.fromEntity(profile));
    }

    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getMyProfile(Authentication authentication) {
        String mobileNumber = authentication.getName();
        EmployerProfile profile = employerProfileService.getByMobileNumber(mobileNumber);
        return ResponseEntity.ok(EmployerProfileResponse.fromEntity(profile));
    }
}
