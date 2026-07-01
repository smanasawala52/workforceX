package com.workforcex.backend.service;

import com.workforcex.backend.dto.MatchedWorkerResponse;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Matching Engine — Spiral 1: rule-based weighted scoring.
 *
 * Formula:
 *   Skills     = 40%
 *   Experience = 30%
 *   Location   = 20%  (worker city matches any of the job's cities)
 *   Salary     = 10%  (worker expectation vs job salary range midpoint)
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

        if (!job.getEmployer().getMobileNumber().equals(employerMobileNumber)) {
            throw new IllegalArgumentException("You do not have permission to access this job");
        }

        return workerProfileRepository.findAll().stream()
                .map(worker -> MatchedWorkerResponse.fromProfile(worker, calculateScore(job, worker)))
                .filter(r -> r.score() > 0)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .collect(Collectors.toList());
    }

    private double calculateScore(Job job, WorkerProfile worker) {
        return (calculateSkillScore(job.getSkillsRequired(), worker.getSkills())     * WEIGHT_SKILLS)
             + (calculateExperienceScore(job.getExperienceRequired(), worker.getExperience()) * WEIGHT_EXPERIENCE)
             + (calculateLocationScore(job.getLocation(), worker.getCity())          * WEIGHT_LOCATION)
             + (calculateSalaryScore(job.getSalaryMin(), job.getSalaryMax(), worker.getPreferredSalary()) * WEIGHT_SALARY);
    }

    /** Skills: proportion of required skills the worker has × 100. */
    private double calculateSkillScore(String jobSkills, String workerSkills) {
        if (jobSkills == null || jobSkills.isBlank()) return 100.0;
        if (workerSkills == null || workerSkills.isBlank()) return 0.0;

        Set<String> required = splitToSet(jobSkills);
        Set<String> has      = splitToSet(workerSkills);
        long matched = required.stream().filter(has::contains).count();
        return (double) matched / required.size() * 100.0;
    }

    /** Experience: meets/exceeds requirement → 100, otherwise proportional. */
    private double calculateExperienceScore(Integer required, Integer workerExp) {
        if (required == null || required == 0) return 100.0;
        if (workerExp == null) return 0.0;
        return workerExp >= required ? 100.0 : (double) workerExp / required * 100.0;
    }

    /**
     * Location: worker's city matches ANY of the job's comma-separated cities → 100.
     * e.g. job = "Mumbai,Pune,Thane", worker city = "Pune" → 100
     */
    private double calculateLocationScore(String jobLocations, String workerCity) {
        if (jobLocations == null || jobLocations.isBlank()) return 100.0;
        if (workerCity == null || workerCity.isBlank()) return 0.0;
        Set<String> cities = splitToSet(jobLocations);
        return cities.contains(workerCity.trim().toLowerCase()) ? 100.0 : 0.0;
    }

    /**
     * Salary: uses midpoint of job's salary range.
     * Worker expectation ≤ midpoint → 100. Above → proportional.
     * If no range defined, full score.
     */
    private double calculateSalaryScore(Double salaryMin, Double salaryMax, Double workerExpected) {
        if (salaryMin == null && salaryMax == null) return 100.0;
        if (workerExpected == null) return 100.0;

        double midpoint = salaryMin != null && salaryMax != null
                ? (salaryMin + salaryMax) / 2.0
                : salaryMin != null ? salaryMin : salaryMax;

        return workerExpected <= midpoint ? 100.0 : midpoint / workerExpected * 100.0;
    }

    private Set<String> splitToSet(String csv) {
        return Arrays.stream(csv.toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
