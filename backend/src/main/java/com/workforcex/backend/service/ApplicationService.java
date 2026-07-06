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
    private final NotificationService notificationService;

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

        // Notify the employer
        notificationService.createNotification(job.getEmployer(), "A new candidate has applied for your job: " + job.getTitle());

        return buildResponse(application);
    }

    // ── Employer: proactively offer a job to a matched worker ───────────────

    public JobApplicationResponse offerJob(String employerMobile, UUID jobId, UUID workerId) {
        User employer = userRepository.findByMobileNumber(employerMobile)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // Verify the employer owns the job
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("You do not have permission to offer this job");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        // Check if an application already exists
        if (applicationRepository.existsByJobIdAndWorkerId(jobId, workerId)) {
            throw new IllegalStateException("An application for this job already exists for this worker.");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setWorker(worker);
        application.setStatus(ApplicationStatus.OFFERED); // Directly set to OFFERED
        applicationRepository.save(application);

        // Notify the worker about the job offer
        String message = String.format(
            "You have received a job offer for '%s' from %s!",
            job.getTitle(),
            employerProfileRepository.findByUserId(employer.getId())
                .map(EmployerProfile::getCompanyName).orElse("a company")
        );
        notificationService.createNotification(worker, message);

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

        if (newStatus == ApplicationStatus.HIRED) {
            Job job = application.getJob();
            if (job.getOpenPositions() > 0) {
                job.setOpenPositions(job.getOpenPositions() - 1);
                jobRepository.save(job);
            } else {
                throw new IllegalStateException("No open positions left for this job");
            }
        }

        application.setStatus(newStatus);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);

        // Notify the worker about the status update
        String message = String.format(
            "Your application for '%s' has been updated to: %s",
            application.getJob().getTitle(),
            newStatus.toString()
        );
        notificationService.createNotification(application.getWorker(), message);

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
