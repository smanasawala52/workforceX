package com.workforcex.backend.service;

import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByMobile(String mobile) {
        return userRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
