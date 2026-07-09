package com.workforcex.backend.service;

import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmployerProfileRepository employerProfileRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;

    @InjectMocks private ApplicationService applicationService;

    private User worker;
    private User employer;
    private Job job;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(UUID.randomUUID());
        worker.setMobileNumber("WORKER_MOBILE");

        employer = new User();
        employer.setId(UUID.randomUUID());
        employer.setMobileNumber("EMPLOYER_MOBILE");

        job = new Job();
        job.setId(UUID.randomUUID());
        job.setEmployerId(employer.getId());
        job.setEmployerMobileNumber(employer.getMobileNumber());
        job.setTitle("Test Job");
        job.setOpenPositions(5);

        application = new JobApplication();
        application.setId(UUID.randomUUID());
        application.setJob(job);
        application.setWorker(worker);
    }

    private void mockProfileRepositories() {
        when(employerProfileRepository.findByUserId(any())).thenReturn(Optional.of(new EmployerProfile()));
        when(workerProfileRepository.findByUserId(any())).thenReturn(Optional.of(new WorkerProfile()));
    }

    @Test
    void apply_shouldCreateApplicationAndNotifyEmployer() {
        mockProfileRepositories();
        when(userRepository.findByMobileNumber("WORKER_MOBILE")).thenReturn(Optional.of(worker));
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(false);
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);

        applicationService.apply("WORKER_MOBILE", job.getId());

        verify(applicationRepository).save(any(JobApplication.class));
        verify(notificationService).createNotification(
            eq(employer),
            eq("A new candidate has applied for your job: Test Job"),
            eq("JOB_APPLICANTS"),
            eq(job.getId())
        );
    }

    @Test
    void offerJob_shouldCreateApplicationAndNotifyWorker() {
        mockProfileRepositories();
        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(userRepository.findById(worker.getId())).thenReturn(Optional.of(worker));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(false);
        // Ensure the save mock returns the application with its ID
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);

        applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId());

        verify(applicationRepository).save(any(JobApplication.class));
        verify(notificationService).createNotification(
            eq(worker),
            anyString(),
            eq("MY_APPLICATIONS"),
            eq(application.getId()) // Verify with the correct, non-null ID
        );
    }

    @Test
    void updateStatus_shouldUpdateStatusAndNotifyWorker() {
        mockProfileRepositories();
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.SHORTLISTED);

        verify(applicationRepository).save(application);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        verify(notificationService).createNotification(
            eq(worker),
            anyString(),
            eq("MY_APPLICATIONS"),
            eq(application.getId())
        );
    }

    // --- Other tests remain the same ---

    @Test
    void apply_shouldThrowException_whenAlreadyApplied() {
        when(userRepository.findByMobileNumber("WORKER_MOBILE")).thenReturn(Optional.of(worker));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply("WORKER_MOBILE", job.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void offerJob_shouldThrowException_whenEmployerDoesNotOwnJob() {
        User anotherEmployer = new User();
        anotherEmployer.setId(UUID.randomUUID());
        job.setEmployerId(anotherEmployer.getId());

        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void offerJob_shouldThrowException_whenApplicationExists() {
        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(userRepository.findById(worker.getId())).thenReturn(Optional.of(worker));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(true);

        assertThatThrownBy(() -> applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateStatus_shouldDecrementPositions_whenHired() {
        mockProfileRepositories();
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.HIRED);

        verify(jobRepository).save(job);
        assertThat(job.getOpenPositions()).isEqualTo(4);
    }

    @Test
    void updateStatus_shouldNotDecrementPositions_whenNotHired() {
        mockProfileRepositories();
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.REJECTED);

        verify(jobRepository, never()).save(any(Job.class));
        assertThat(job.getOpenPositions()).isEqualTo(5);
    }

    @Test
    void updateStatus_shouldThrowException_whenHiringForJobWithNoOpenPositions() {
        job.setOpenPositions(0);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        assertThatThrownBy(() -> applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.HIRED))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateStatus_shouldThrowException_whenEmployerDoesNotOwnJob() {
        User anotherEmployer = new User();
        anotherEmployer.setMobileNumber("ANOTHER_EMPLOYER");
        job.setEmployerId(anotherEmployer.getId());

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(userRepository.findById(anotherEmployer.getId())).thenReturn(Optional.of(anotherEmployer));

        assertThatThrownBy(() -> applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.SHORTLISTED))
                .isInstanceOf(ResponseStatusException.class);
    }
}
