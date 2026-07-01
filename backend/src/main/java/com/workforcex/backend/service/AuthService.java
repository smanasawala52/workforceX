package com.workforcex.backend.service;

import com.workforcex.backend.dto.LoginRequest;
import com.workforcex.backend.dto.RegisterRequest;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        User user = new User();
        user.setCountryCode(request.countryCode()); // defaults to +91 via DTO constructor
        user.setMobileNumber(request.mobileNumber());
        user.setPassword(passwordEncoder.encode(request.mobileNumber())); // dev: password = mobile number
        user.setRole(request.role());

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByMobileNumber(request.mobileNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid mobile number or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid mobile number or password");
        }

        return user;
    }
}
