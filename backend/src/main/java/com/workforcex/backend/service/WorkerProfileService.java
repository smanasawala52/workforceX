package com.workforcex.backend.service;

import com.workforcex.backend.dto.WorkerProfileRequest;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;

    /**
     * Creates the profile if one doesn't exist yet, otherwise updates it.
     * mobileNumber comes from the authenticated JWT, never from the request body.
     */
    public WorkerProfile saveOrUpdate(String mobileNumber, WorkerProfileRequest request) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkerProfile profile = workerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WorkerProfile newProfile = new WorkerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setEmail(request.email());
        profile.setAddress(request.address());
        profile.setCity(request.city());
        profile.setState(request.state());
        profile.setSkills(request.skills());
        profile.setExperience(request.experience());
        profile.setPreferredSalary(request.preferredSalary());

        return workerProfileRepository.save(profile);
    }

    public WorkerProfile getByMobileNumber(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return workerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found - complete your profile first"));
    }
}
