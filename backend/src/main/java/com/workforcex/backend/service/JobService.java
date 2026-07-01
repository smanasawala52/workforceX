package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobRequest;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Job createJob(String employerMobileNumber, JobRequest request) {
        User employer = userRepository.findByMobileNumber(employerMobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Job job = new Job();
        job.setEmployer(employer);
        applyRequest(job, request);
        return jobRepository.save(job);
    }

    public Job updateJob(String employerMobileNumber, UUID jobId, JobRequest request) {
        Job job = getOwnedJob(employerMobileNumber, jobId);
        applyRequest(job, request);
        return jobRepository.save(job);
    }

    public void deleteJob(String employerMobileNumber, UUID jobId) {
        jobRepository.delete(getOwnedJob(employerMobileNumber, jobId));
    }

    public Job getJobById(String employerMobileNumber, UUID jobId) {
        return getOwnedJob(employerMobileNumber, jobId);
    }

    public List<Job> getJobsForEmployer(String employerMobileNumber) {
        User employer = userRepository.findByMobileNumber(employerMobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jobRepository.findAllByEmployerId(employer.getId());
    }

    private Job getOwnedJob(String employerMobileNumber, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (!job.getEmployer().getMobileNumber().equals(employerMobileNumber)) {
            throw new IllegalArgumentException("You do not have permission to access this job");
        }
        return job;
    }

    private void applyRequest(Job job, JobRequest request) {
        job.setTitle(request.title());
        job.setSkillsRequired(request.skillsRequired());
        job.setExperienceRequired(request.experienceRequired());
        job.setLocation(request.location());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setOpenPositions(request.openPositions());
        job.setDescription(request.description());
    }
}
