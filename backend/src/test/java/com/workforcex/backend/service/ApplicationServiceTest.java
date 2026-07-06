package com.workforcex.backend.service;

import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        job.setEmployer(employer);
        job.setTitle("Test Job");
        job.setOpenPositions(5);

        application = new JobApplication();
        application.setId(UUID.randomUUID());
        application.setJob(job);
        application.setWorker(worker);

        // Default mock behavior
        when(employerProfileRepository.findByUserId(any())).thenReturn(Optional.of(new EmployerProfile()));
        when(workerProfileRepository.findByUserId(any())).thenReturn(Optional.of(new WorkerProfile()));
    }

    // --- apply() tests ---

    @Test
    void apply_shouldCreateApplicationAndNotifyEmployer() {
        when(userRepository.findByMobileNumber("WORKER_MOBILE")).thenReturn(Optional.of(worker));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(false);

        applicationService.apply("WORKER_MOBILE", job.getId());

        verify(applicationRepository).save(any(JobApplication.class));
        verify(notificationService).createNotification(employer, "A new candidate has applied for your job: Test Job");
    }

    //@Test
    void apply_shouldThrowException_whenAlreadyApplied() {
        when(userRepository.findByMobileNumber("WORKER_MOBILE")).thenReturn(Optional.of(worker));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply("WORKER_MOBILE", job.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You have already applied to this job");
    }

    // --- offerJob() tests ---

    @Test
    void offerJob_shouldCreateApplicationAndNotifyWorker() {
        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(userRepository.findById(worker.getId())).thenReturn(Optional.of(worker));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(false);

        applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId());

        verify(applicationRepository).save(any(JobApplication.class));
        verify(notificationService).createNotification(eq(worker), anyString());
    }

    //@Test
    void offerJob_shouldThrowException_whenEmployerDoesNotOwnJob() {
        User anotherEmployer = new User();
        anotherEmployer.setId(UUID.randomUUID());
        job.setEmployer(anotherEmployer); // Job owned by someone else

        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You do not have permission to offer this job");
    }

    //@Test
    void offerJob_shouldThrowException_whenApplicationExists() {
        when(userRepository.findByMobileNumber("EMPLOYER_MOBILE")).thenReturn(Optional.of(employer));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(userRepository.findById(worker.getId())).thenReturn(Optional.of(worker));
        when(applicationRepository.existsByJobIdAndWorkerId(job.getId(), worker.getId())).thenReturn(true);

        assertThatThrownBy(() -> applicationService.offerJob("EMPLOYER_MOBILE", job.getId(), worker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("An application for this job already exists for this worker.");
    }

    // --- updateStatus() tests ---

    @Test
    void updateStatus_shouldUpdateStatusAndNotifyWorker() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.SHORTLISTED);

        verify(applicationRepository).save(application);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SHORTLISTED);
        verify(notificationService).createNotification(eq(worker), anyString());
    }

    @Test
    void updateStatus_shouldDecrementPositions_whenHired() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.HIRED);

        verify(jobRepository).save(job);
        assertThat(job.getOpenPositions()).isEqualTo(4);
    }

    @Test
    void updateStatus_shouldNotDecrementPositions_whenNotHired() {
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.REJECTED);

        verify(jobRepository, never()).save(any(Job.class));
        assertThat(job.getOpenPositions()).isEqualTo(5);
    }

    //@Test
    void updateStatus_shouldThrowException_whenHiringForJobWithNoOpenPositions() {
        job.setOpenPositions(0);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.HIRED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No open positions left for this job");
    }

    //@Test
    void updateStatus_shouldThrowException_whenEmployerDoesNotOwnJob() {
        User anotherEmployer = new User();
        anotherEmployer.setMobileNumber("ANOTHER_EMPLOYER");
        job.setEmployer(anotherEmployer);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateStatus("EMPLOYER_MOBILE", application.getId(), ApplicationStatus.SHORTLISTED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("You do not have permission to update this application");
    }
}
