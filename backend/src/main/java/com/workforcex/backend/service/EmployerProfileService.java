package com.workforcex.backend.service;

import com.workforcex.backend.dto.EmployerProfileRequest;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;

    public EmployerProfile saveOrUpdate(String mobileNumber, EmployerProfileRequest request) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    EmployerProfile newProfile = new EmployerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setCompanyName(request.companyName());
        profile.setContactPerson(request.contactPerson());
        profile.setEmail(request.email());
        profile.setAddress(request.address());

        return employerProfileRepository.save(profile);
    }

    public EmployerProfile getByMobileNumber(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return employerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employer profile not found - complete your profile first"));
    }
}
