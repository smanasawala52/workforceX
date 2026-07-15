package com.workforcex.backend.service;

import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.JobApplicationRepository;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;
    @Mock private JobApplicationRepository applicationRepository;

    @InjectMocks private MatchingService matchingService;

    @Test
    void getMatchedWorkers_shouldReturnEmptyList_whenNoWorkersExist() {
        // Given
        UUID jobId = UUID.randomUUID();
        String countryCode = "+91";
        User employer = new User();
        employer.setId(UUID.randomUUID());
        employer.setCountryCode(countryCode);
        employer.setMobileNumber("1234567890");
        Job job = new Job();
        job.setEmployerId(employer.getId());
        job.setEmployerMobileNumber(employer.getMobileNumber());


        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findByCountryCodeAndMobileNumber(countryCode, "1234567890")).thenReturn(Optional.of(employer));
        when(workerProfileRepository.findMatchingWorkers(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        var result = matchingService.getMatchedWorkers(countryCode, "1234567890", jobId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
