package com.workforcex.backend.service;

import com.workforcex.backend.dto.UserResponse;
import com.workforcex.backend.entity.Admin;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.AdminRepository;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserQueryService userQueryService;

    public List<UserResponse> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mobileNumber = authentication.getName();
        User currentUser = userRepository.findByFullMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Admin admin = adminRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return userQueryService.getUsersByRegion(admin.getRegion());
    }
}
