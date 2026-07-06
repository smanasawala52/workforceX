package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobApplicationResponse;
import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    // ── Worker: apply to a job ────────────────────────────────────────────────

    public JobApplicationResponse apply(String workerMobile, UUID jobId) {
        User worker = userRepository.findByMobileNumber(workerMobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (applicationRepository.existsByJobIdAndWorkerId(jobId, worker.getId())) {
            throw new IllegalArgumentException("You have already applied to this job");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setWorker(worker);
        application.setStatus(ApplicationStatus.PENDING);

        applicationRepository.save(application);
        return buildResponse(application);
    }

    // ── Worker: view my applications ──────────────────────────────────────────

    public List<JobApplicationResponse> getMyApplications(String workerMobile) {
        User worker = userRepository.findByMobileNumber(workerMobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return applicationRepository.findAllByWorkerId(worker.getId()).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    // ── Employer: view applicants for a job ──────────────────────────────────

    public List<JobApplicationResponse> getApplicationsForJob(String employerMobile, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getEmployer().getMobileNumber().equals(employerMobile)) {
            throw new IllegalArgumentException("You do not have permission to view this job's applications");
        }

        return applicationRepository.findAllByJobId(jobId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    // ── Employer: shortlist or reject ─────────────────────────────────────────

    public JobApplicationResponse updateStatus(String employerMobile, UUID applicationId, ApplicationStatus newStatus) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify the employer owns the job this application is for
        if (!application.getJob().getEmployer().getMobileNumber().equals(employerMobile)) {
            throw new IllegalArgumentException("You do not have permission to update this application");
        }

        application.setStatus(newStatus);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);
        return buildResponse(application);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JobApplicationResponse buildResponse(JobApplication app) {
        // Resolve company name
        String companyName = employerProfileRepository
                .findByUserId(app.getJob().getEmployer().getId())
                .map(EmployerProfile::getCompanyName)
                .orElse("Unknown Company");

        // Resolve worker profile details
        var profile = workerProfileRepository.findByUserId(app.getWorker().getId());
        String name    = profile.map(WorkerProfile::getName).orElse(null);
        String skills  = profile.map(WorkerProfile::getSkills).orElse(null);
        Integer exp    = profile.map(WorkerProfile::getExperience).orElse(null);
        String city    = profile.map(WorkerProfile::getCity).orElse(null);
        Double salary  = profile.map(WorkerProfile::getPreferredSalary).orElse(null);

        return JobApplicationResponse.fromEntityWithProfile(
                app, companyName, name, skills, exp, city, salary);
    }
}
