package com.workforcex.backend.service;

import com.workforcex.backend.dto.EmployerProfileRequest;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;

    private User findUserByFullMobile(String fullMobile) {
        String countryCode;
        String mobileNumber;
        if (fullMobile.startsWith("+91")) {
            countryCode = "+91";
            mobileNumber = fullMobile.substring(3);
        } else if (fullMobile.startsWith("+971")) {
            countryCode = "+971";
            mobileNumber = fullMobile.substring(4);
        } else {
            countryCode = "+91"; // Fallback
            mobileNumber = fullMobile;
        }
        return userRepository.findByCountryCodeAndMobileNumber(countryCode, mobileNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public EmployerProfile saveOrUpdate(String mobileNumber, EmployerProfileRequest request) {
        User user = findUserByFullMobile(mobileNumber);

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
        User user = findUserByFullMobile(mobileNumber);

        return employerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employer profile not found - complete your profile first"));
    }
}
