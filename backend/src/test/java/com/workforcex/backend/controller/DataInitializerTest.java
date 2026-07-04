package com.workforcex.backend.controller;

import com.workforcex.backend.config.DataInitializer;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests DataInitializer in isolation — NOT using "test" profile
 * so DataInitializer bean is actually available.
 * We call it manually to verify seeding logic.
 */
@SpringBootTest
@ActiveProfiles("test") // keeps other aspects isolated
class DataInitializerTest {

    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployerProfileRepository employerProfileRepository;
    @Autowired private WorkerProfileRepository workerProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        jobRepository.deleteAll();
        workerProfileRepository.deleteAll();
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void dataInitializer_seeds200Jobs_across10Employers() throws Exception {
        DataInitializer initializer = new DataInitializer(
                userRepository, employerProfileRepository,
                jobRepository, passwordEncoder);

        // Simulate startup
        initializer.run(null);

        assertThat(jobRepository.count()).isEqualTo(200);
        assertThat(userRepository.count()).isEqualTo(10); // 10 employer accounts
        assertThat(employerProfileRepository.count()).isEqualTo(10);
    }

    @Test
    void dataInitializer_doesNotSeedTwice_whenJobsAlreadyExist() throws Exception {
        DataInitializer initializer = new DataInitializer(
                userRepository, employerProfileRepository,
                jobRepository, passwordEncoder);

        initializer.run(null); // first run — seeds
        initializer.run(null); // second run — should skip

        assertThat(jobRepository.count()).isEqualTo(200); // not 400
    }
}
