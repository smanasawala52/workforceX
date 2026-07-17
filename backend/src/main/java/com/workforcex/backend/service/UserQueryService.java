package com.workforcex.backend.service;

import com.workforcex.backend.dto.UserResponse;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public List<UserResponse> getUsersByRegion(String region) {
        if (region == null) {
            // Return all users
            return userRepository.findAll().stream()
                    .map(UserResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            // Filter by region
            List<User> workersInRegion = workerProfileRepository.findByCity(region).stream()
                    .map(profile -> userRepository.findById(profile.getUserId()).orElse(null))
                    .collect(Collectors.toList());

            List<User> employersInRegion = employerProfileRepository.findByAddressContaining(region).stream()
                    .map(EmployerProfile::getUser)
                    .collect(Collectors.toList());

            return Stream.concat(workersInRegion.stream(), employersInRegion.stream())
                    .filter(user -> user != null)
                    .map(UserResponse::fromEntity)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}
