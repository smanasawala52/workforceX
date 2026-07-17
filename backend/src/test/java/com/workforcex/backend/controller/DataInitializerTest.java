package com.workforcex.backend.controller;

import com.workforcex.backend.config.DataInitializer;
import com.workforcex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
    @Autowired private SkillRepository skillRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private CSRRepository csrRepository;

    @BeforeEach
    void clean() {
        jobRepository.deleteAll();
        workerProfileRepository.deleteAll();
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void dataInitializer_seedsData() throws Exception {
        DataInitializer initializer = new DataInitializer(
                userRepository, employerProfileRepository,
                jobRepository, workerProfileRepository, passwordEncoder, skillRepository, adminRepository, csrRepository);

        // Simulate startup
        initializer.run(null);

        assertThat(jobRepository.count()).isEqualTo(200);
        assertThat(userRepository.count()).isEqualTo(35); // 10 employers + 20 workers + 5 admins/csrs
        assertThat(employerProfileRepository.count()).isEqualTo(10);
        assertThat(workerProfileRepository.count()).isEqualTo(20);
    }

    @Test
    void dataInitializer_doesNotSeedTwice() throws Exception {
        DataInitializer initializer = new DataInitializer(
                userRepository, employerProfileRepository,
                jobRepository, workerProfileRepository, passwordEncoder,skillRepository, adminRepository, csrRepository);

        initializer.run(null); // first run — seeds
        initializer.run(null); // second run — should skip

        assertThat(jobRepository.count()).isEqualTo(200); // not 400
        assertThat(workerProfileRepository.count()).isEqualTo(20);
    }
}
