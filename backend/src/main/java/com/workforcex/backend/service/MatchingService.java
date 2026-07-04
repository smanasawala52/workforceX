package com.workforcex.backend.service;

import com.workforcex.backend.dto.CandidateSearchRequest;
import com.workforcex.backend.dto.CandidateSearchResponse;
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
 * Matching Engine — Spiral 1 + Spiral 2 search.
 *
 * Formula (both job-match and free search):
 *   Skills     = 40%
 *   Experience = 30%
 *   Location   = 20%
 *   Salary     = 10%
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

    // ── Spiral 1: match workers to a specific job ─────────────────────────────

    public List<MatchedWorkerResponse> getMatchedWorkers(String employerMobileNumber, UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getEmployer().getMobileNumber().equals(employerMobileNumber)) {
            throw new IllegalArgumentException("You do not have permission to access this job");
        }

        return workerProfileRepository.findAll().stream()
                .map(worker -> {
                    Scores s = score(
                            job.getSkillsRequired(), worker.getSkills(),
                            job.getExperienceRequired(), worker.getExperience(),
                            job.getLocation(), worker.getCity(),
                            job.getSalaryMin(), job.getSalaryMax(), worker.getPreferredSalary()
                    );
                    return MatchedWorkerResponse.fromProfile(worker, s.total());
                })
                .filter(r -> r.score() > 0)
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .collect(Collectors.toList());
    }

    // ── Spiral 2: free-text search with filters ───────────────────────────────

    /**
     * Search all workers using optional filters.
     * Hard filters (city, experience range, salary range) eliminate candidates.
     * Skill score drives ranking among those who pass.
     */
    public List<CandidateSearchResponse> search(CandidateSearchRequest request) {
        return workerProfileRepository.findAll().stream()
                .filter(worker -> passesHardFilters(worker, request))
                .map(worker -> {
                    Scores s = score(
                            request.skills(), worker.getSkills(),
                            request.experienceMin(), worker.getExperience(),
                            request.city(), worker.getCity(),
                            request.salaryMin(), request.salaryMax(), worker.getPreferredSalary()
                    );
                    return CandidateSearchResponse.fromProfile(
                            worker, s.total(), s.skill(), s.experience(), s.location(), s.salary()
                    );
                })
                .filter(r -> r.totalScore() > 0)
                .sorted((a, b) -> Double.compare(b.totalScore(), a.totalScore()))
                .collect(Collectors.toList());
    }

    // ── Hard filters (eliminate, not score) ──────────────────────────────────

    private boolean passesHardFilters(WorkerProfile worker, CandidateSearchRequest req) {
        // Skills filter — worker must have AT LEAST ONE of the required skills
        if (req.skills() != null && !req.skills().isBlank()) {
            if (worker.getSkills() == null || worker.getSkills().isBlank()) return false;
            Set<String> required = splitToSet(req.skills());
            Set<String> has = splitToSet(worker.getSkills());
            boolean anyMatch = required.stream().anyMatch(has::contains);
            if (!anyMatch) return false;
        }
        // City filter
        if (req.city() != null && !req.city().isBlank()) {
            if (worker.getCity() == null) return false;
            if (!worker.getCity().trim().equalsIgnoreCase(req.city().trim())) return false;
        }
        // Experience range filter
        if (req.experienceMin() != null && worker.getExperience() != null
                && worker.getExperience() < req.experienceMin()) return false;
        if (req.experienceMax() != null && worker.getExperience() != null
                && worker.getExperience() > req.experienceMax()) return false;
        // Salary range filter
        if (req.salaryMin() != null && worker.getPreferredSalary() != null
                && worker.getPreferredSalary() < req.salaryMin()) return false;
        if (req.salaryMax() != null && worker.getPreferredSalary() != null
                && worker.getPreferredSalary() > req.salaryMax()) return false;
        return true;
    }

    // ── Shared scoring ────────────────────────────────────────────────────────

    private record Scores(double skill, double experience, double location, double salary) {
        double total() {
            return skill * WEIGHT_SKILLS
                    + experience * WEIGHT_EXPERIENCE
                    + location * WEIGHT_LOCATION
                    + salary * WEIGHT_SALARY;
        }
    }

    private Scores score(
            String jobSkills, String workerSkills,
            Integer requiredExp, Integer workerExp,
            String jobLocation, String workerCity,
            Double salaryMin, Double salaryMax, Double workerExpectedSalary
    ) {
        return new Scores(
                skillScore(jobSkills, workerSkills),
                experienceScore(requiredExp, workerExp),
                locationScore(jobLocation, workerCity),
                salaryScore(salaryMin, salaryMax, workerExpectedSalary)
        );
    }

    private double skillScore(String jobSkills, String workerSkills) {
        if (jobSkills == null || jobSkills.isBlank()) return 100.0;
        if (workerSkills == null || workerSkills.isBlank()) return 0.0;
        Set<String> required = splitToSet(jobSkills);
        Set<String> has = splitToSet(workerSkills);
        long matched = required.stream().filter(has::contains).count();
        return (double) matched / required.size() * 100.0;
    }

    private double experienceScore(Integer required, Integer workerExp) {
        if (required == null || required == 0) return 100.0;
        if (workerExp == null) return 0.0;
        return workerExp >= required ? 100.0 : (double) workerExp / required * 100.0;
    }

    private double locationScore(String jobLocation, String workerCity) {
        if (jobLocation == null || jobLocation.isBlank()) return 100.0;
        if (workerCity == null || workerCity.isBlank()) return 0.0;
        Set<String> cities = splitToSet(jobLocation);
        return cities.contains(workerCity.trim().toLowerCase()) ? 100.0 : 0.0;
    }

    private double salaryScore(Double salaryMin, Double salaryMax, Double workerExpected) {
        if (workerExpected == null) return 100.0;
        if (salaryMin == null && salaryMax == null) return 100.0;
        double midpoint = (salaryMin != null && salaryMax != null)
                ? (salaryMin + salaryMax) / 2.0
                : salaryMin != null ? salaryMin : salaryMax;
        return workerExpected <= midpoint ? 100.0 : midpoint / workerExpected * 100.0;
    }

    private Set<String> splitToSet(String csv) {
        return Arrays.stream(csv.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
