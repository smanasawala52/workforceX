package com.workforcex.backend.service;

import com.workforcex.backend.dto.MatchedWorkerResponse;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Matching Engine — Spiral 1: rule-based weighted scoring only.
 *
 * Formula:
 *   Skills     = 40%
 *   Experience = 30%
 *   Location   = 20%
 *   Salary     = 10%
 *
 * Each component scores 0–100, multiplied by its weight.
 * Result is a final score 0–100, sorted highest first.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final double WEIGHT_SKILLS     = 0.40;
    private static final double WEIGHT_EXPERIENCE = 0.30;
    private static final double WEIGHT_LOCATION   = 0.20;
    private static final double WEIGHT_SALARY     = 0.10;

    private final JobRepository jobRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public List<MatchedWorkerResponse> getMatchedWorkers(String employerMobileNumber, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        // Verify the requesting employer owns this job
        if (!job.getEmployer().getMobileNumber().equals(employerMobileNumber)) {
            throw new IllegalArgumentException("You do not have permission to access this job");
        }

        List<WorkerProfile> allWorkers = workerProfileRepository.findAll();

        return allWorkers.stream()
                .map(worker -> {
                    double score = calculateScore(job, worker);
                    return MatchedWorkerResponse.fromProfile(worker, score);
                })
                .filter(result -> result.score() > 0) // exclude workers with 0 match
                .sorted((a, b) -> Double.compare(b.score(), a.score())) // highest first
                .collect(Collectors.toList());
    }

    private double calculateScore(Job job, WorkerProfile worker) {
        double skillScore      = calculateSkillScore(job.getSkillsRequired(), worker.getSkills());
        double experienceScore = calculateExperienceScore(job.getExperienceRequired(), worker.getExperience());
        double locationScore   = calculateLocationScore(job.getLocation(), worker.getCity());
        double salaryScore     = calculateSalaryScore(job.getSalary(), worker.getPreferredSalary());

        return (skillScore * WEIGHT_SKILLS)
                + (experienceScore * WEIGHT_EXPERIENCE)
                + (locationScore * WEIGHT_LOCATION)
                + (salaryScore * WEIGHT_SALARY);
    }

    /**
     * Skills: count how many required skills the worker has.
     * 100 if worker has all required skills, proportional otherwise.
     * If no skills required on the job, full score for everyone.
     */
    private double calculateSkillScore(String jobSkills, String workerSkills) {
        if (jobSkills == null || jobSkills.isBlank()) return 100.0;
        if (workerSkills == null || workerSkills.isBlank()) return 0.0;

        Set<String> required = Arrays.stream(jobSkills.toLowerCase().split(","))
                .map(String::trim).collect(Collectors.toSet());
        Set<String> workerHas = Arrays.stream(workerSkills.toLowerCase().split(","))
                .map(String::trim).collect(Collectors.toSet());

        long matched = required.stream().filter(workerHas::contains).count();
        return (double) matched / required.size() * 100.0;
    }

    /**
     * Experience: worker meets or exceeds requirement → 100.
     * Below requirement → proportional score (e.g. 2yrs actual / 5yrs required = 40).
     * If no experience required, everyone gets full score.
     */
    private double calculateExperienceScore(Integer required, Integer workerExperience) {
        if (required == null || required == 0) return 100.0;
        if (workerExperience == null) return 0.0;
        if (workerExperience >= required) return 100.0;
        return (double) workerExperience / required * 100.0;
    }

    /**
     * Location: exact city match (case-insensitive) → 100, otherwise 0.
     * Simple for Spiral 1 — can improve to state/region matching in later spirals.
     */
    private double calculateLocationScore(String jobLocation, String workerCity) {
        if (jobLocation == null || jobLocation.isBlank()) return 100.0;
        if (workerCity == null || workerCity.isBlank()) return 0.0;
        return jobLocation.trim().equalsIgnoreCase(workerCity.trim()) ? 100.0 : 0.0;
    }

    /**
     * Salary: worker's expectation within or below the offered salary → 100.
     * Above → proportional (e.g. worker wants 20k, job offers 15k → 75%).
     * If no salary on either side, full score.
     */
    private double calculateSalaryScore(Double offeredSalary, Double workerExpected) {
        if (offeredSalary == null || workerExpected == null) return 100.0;
        if (workerExpected <= offeredSalary) return 100.0;
        return offeredSalary / workerExpected * 100.0;
    }
}
