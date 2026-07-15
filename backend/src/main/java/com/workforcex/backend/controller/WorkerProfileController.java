package com.workforcex.backend.controller;

import com.workforcex.backend.dto.WorkerProfileRequest;
import com.workforcex.backend.dto.WorkerProfileResponse;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.service.WorkerProfileService;
import com.workforcex.backend.util.PhoneNumbers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/worker/profile")
@RequiredArgsConstructor
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    @PutMapping
    public ResponseEntity<WorkerProfileResponse> saveOrUpdate(
            Authentication authentication,
            @RequestBody WorkerProfileRequest request
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(authentication.getName()); // set by JwtAuthFilter from the token's subject
        WorkerProfile profile = workerProfileService.saveOrUpdate(split.countryCode(), split.mobileNumber(), request);
        return ResponseEntity.ok(WorkerProfileResponse.fromEntity(profile));
    }

    @GetMapping
    public ResponseEntity<WorkerProfileResponse> getMyProfile(
            Authentication authentication
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(authentication.getName());
        WorkerProfile profile = workerProfileService.getByMobileNumber(split.countryCode(), split.mobileNumber());
        return ResponseEntity.ok(WorkerProfileResponse.fromEntity(profile));
    }

    @GetMapping("/{workerId}")
    public ResponseEntity<WorkerProfileResponse> getWorkerProfileById(@PathVariable UUID workerId) {
        WorkerProfile profile = workerProfileService.getByUserId(workerId);
        return ResponseEntity.ok(WorkerProfileResponse.fromEntity(profile));
    }
}
