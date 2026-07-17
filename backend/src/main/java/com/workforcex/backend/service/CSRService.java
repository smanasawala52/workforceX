package com.workforcex.backend.service;

import com.workforcex.backend.dto.UserResponse;
import com.workforcex.backend.entity.CSR;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.CSRRepository;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CSRService {

    private final UserRepository userRepository;
    private final CSRRepository csrRepository;
    private final UserQueryService userQueryService;

    public List<UserResponse> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mobileNumber = authentication.getName();
        User currentUser = userRepository.findByFullMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CSR csr = csrRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("CSR not found"));

        return userQueryService.getUsersByRegion(csr.getRegion());
    }
}
