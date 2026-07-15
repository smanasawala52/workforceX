package com.workforcex.backend.service;

import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByMobile(String fullMobile) {
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
}
