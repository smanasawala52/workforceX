package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobApplicationResponse;
import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final NotificationService notificationService;

    public JobApplicationResponse apply(String workerMobile, UUID jobId) {
        User worker = userRepository.findByMobileNumber(workerMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (applicationRepository.existsByJobIdAndWorkerId(jobId, worker.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied to this job");
        }

        JobApplication applicationToSave = new JobApplication();
        applicationToSave.setJob(job);
        applicationToSave.setWorker(worker);
        applicationToSave.setStatus(ApplicationStatus.PENDING);
        JobApplication savedApplication = applicationRepository.save(applicationToSave);
        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        notificationService.createNotification(
                employer,
            "A new candidate has applied for your job: " + job.getTitle(),
            "JOB_APPLICANTS",
            job.getId()
        );
        return buildResponse(savedApplication);
    }

    public JobApplicationResponse offerJob(String employerMobile, UUID jobId, UUID workerId) {
        User employer = userRepository.findByMobileNumber(employerMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!job.getEmployerId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to offer this job");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));

        if (applicationRepository.existsByJobIdAndWorkerId(jobId, workerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An application for this job already exists for this worker.");
        }

        JobApplication applicationToSave = new JobApplication();
        applicationToSave.setJob(job);
        applicationToSave.setWorker(worker);
        applicationToSave.setStatus(ApplicationStatus.OFFERED);
        JobApplication savedApplication = applicationRepository.save(applicationToSave);

        String companyName = employerProfileRepository.findByUserId(employer.getId())
                .map(EmployerProfile::getCompanyName).orElse("a company");
        String message = String.format("You have received a job offer for '%s' from %s!", job.getTitle(), companyName);
        notificationService.createNotification(worker, message, "MY_APPLICATIONS", savedApplication.getId());

        return buildResponse(savedApplication);
    }

    public List<JobApplicationResponse> getMyApplications(String workerMobile) {
        User worker = userRepository.findByMobileNumber(workerMobile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return applicationRepository.findAllByWorkerId(worker.getId()).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public List<JobApplicationResponse> getApplicationsForJob(String employerMobile, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!employer.getMobileNumber().equals(employerMobile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have permission to view this job's applications");
        }
        return applicationRepository.findAllByJobId(jobId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public JobApplicationResponse updateStatus(String employerMobile, UUID applicationId, ApplicationStatus newStatus) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        User employer = userRepository.findById(application.getJob().getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!employer.getMobileNumber().equals(employerMobile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this application");
        }

        if (newStatus == ApplicationStatus.HIRED) {
            Job job = application.getJob();
            if (job.getOpenPositions() > 0) {
                job.setOpenPositions(job.getOpenPositions() - 1);
                jobRepository.save(job);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No open positions left for this job");
            }
        }

        application.setStatus(newStatus);
        application.setUpdatedAt(LocalDateTime.now());
        JobApplication savedApplication = applicationRepository.save(application);

        String companyName = employerProfileRepository.findByUserId(savedApplication.getJob().getEmployerId())
                .map(EmployerProfile::getCompanyName).orElse("a company");
        String message = String.format("Your application for '%s' at %s has been updated to: %s",
                savedApplication.getJob().getTitle(), companyName, newStatus.toString());
        notificationService.createNotification(savedApplication.getWorker(), message, "MY_APPLICATIONS", savedApplication.getId());

        return buildResponse(savedApplication);
    }

    private JobApplicationResponse buildResponse(JobApplication app) {
        String companyName = employerProfileRepository.findByUserId(app.getJob().getEmployerId())
                .map(EmployerProfile::getCompanyName).orElse("Unknown Company");
        var profile = workerProfileRepository.findByUserId(app.getWorker().getId());
        return JobApplicationResponse.fromEntityWithProfile(
                app, companyName,
                profile.map(WorkerProfile::getName).orElse(null),
                profile.map(wp -> this.getMergedSkills(wp.getSkill1(), wp.getSkill2(), wp.getSkill3(), wp.getSkill4(), wp.getSkill5())).orElse(null),
                profile.map(WorkerProfile::getExperience).orElse(null),
                profile.map(WorkerProfile::getCity).orElse(null),
                profile.map(WorkerProfile::getPreferredSalary).orElse(null)
        );
    }
    // Helper method to merge skills from WorkerProfile
    public String getMergedSkills(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.joining(","));
    }
}
