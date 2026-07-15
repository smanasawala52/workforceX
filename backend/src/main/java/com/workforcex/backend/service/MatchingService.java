package com.workforcex.backend.service;

import com.workforcex.backend.dto.CandidateSearchRequest;
import com.workforcex.backend.dto.CandidateSearchResponse;
import com.workforcex.backend.dto.MatchedWorkerResponse;
import com.workforcex.backend.entity.*;
import com.workforcex.backend.repository.JobApplicationRepository;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private static final double WEIGHT_SKILLS     = 0.40;
    private static final double WEIGHT_EXPERIENCE = 0.30;
    private static final double WEIGHT_LOCATION   = 0.20;
    private static final double WEIGHT_SALARY     = 0.10;

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final JobApplicationRepository applicationRepository;

    public List<MatchedWorkerResponse> getMatchedWorkers(
            String countryCode,
            String employerMobileNumber,
            UUID jobId
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        User employer = userRepository.findByCountryCodeAndMobileNumber(countryCode, employerMobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!job.getEmployerId().equals(employer.getId())) {
            throw new IllegalArgumentException(
                    "You do not have permission to access this job"
            );
        }
        // Fetch only possible candidates from database
        List<WorkerProfile> workers =
                workerProfileRepository.findMatchingWorkers(
                        splitToSet(job.getLocation()),
                        job.getExperienceRequired(),
                        job.getSalaryMin(),
                        job.getSalaryMax(),
                        getMergedSkillsSet(job.getSkillsRequired1(),
                                job.getSkillsRequired2(),
                                job.getSkillsRequired3(),
                                job.getSkillsRequired4(),
                                job.getSkillsRequired5())
                );
        return workers.stream()
                .map(worker -> {

                    Scores s = score(
                            getMergedSkillsSet(job.getSkillsRequired1(),
                                    job.getSkillsRequired2(),
                                    job.getSkillsRequired3(),
                                    job.getSkillsRequired4(),
                                    job.getSkillsRequired5()),
                            getMergedSkillsSet(worker.getSkill1(),
                                    worker.getSkill2(),
                                    worker.getSkill3(),
                                    worker.getSkill4(),
                                    worker.getSkill5()),
                            job.getExperienceRequired(),
                            worker.getExperience(),
                            job.getLocation(),
                            worker.getCity(),
                            job.getSalaryMin(),
                            job.getSalaryMax(),
                            worker.getPreferredSalary()
                    );


                    /*ApplicationStatus status = applicationRepository
                            .findByJobIdAndWorkerId(jobId, worker.getUserId())
                            .map(JobApplication::getStatus)
                            .orElse(null);

                     */


                    return MatchedWorkerResponse.fromProfile(
                            worker,
                            s.total(),
                            ApplicationStatus.PENDING
                    );
                })
                .filter(worker -> worker.score() > 0)
                .sorted(
                        Comparator.comparingDouble(
                                MatchedWorkerResponse::score
                        ).reversed()
                )
                .collect(Collectors.toList());
    }
    public List<CandidateSearchResponse> search(CandidateSearchRequest request) {

        List<WorkerProfile> workers =
                workerProfileRepository.searchCandidates(
                        splitToSet(request.city()),
                        request.experienceMin(),
                        request.experienceMax(),
                        request.salaryMin(),
                        request.salaryMax(),
                        splitToSet(request.skills())
                );

        return workers.stream()
                .map(worker -> {

                    Scores s = score(
                            splitToSet(request.skills()),
                            getMergedSkillsSet(worker.getSkill1(),
                                    worker.getSkill2(),
                                    worker.getSkill3(),
                                    worker.getSkill4(),
                                    worker.getSkill5()),
                            request.experienceMin(),
                            worker.getExperience(),
                            request.city(),
                            worker.getCity(),
                            request.salaryMin(),
                            request.salaryMax(),
                            worker.getPreferredSalary()
                    );

                    return CandidateSearchResponse.fromProfile(
                            worker,
                            s.total(),
                            s.skill(),
                            s.experience(),
                            s.location(),
                            s.salary()
                    );
                })
                .sorted((a,b) ->
                        Double.compare(
                                b.totalScore(),
                                a.totalScore()
                        )
                )
                .toList();
    }

    private record Scores(double skill, double experience, double location, double salary) {
        double total() {
            return skill * WEIGHT_SKILLS
                    + experience * WEIGHT_EXPERIENCE
                    + location * WEIGHT_LOCATION
                    + salary * WEIGHT_SALARY;
        }
    }

    private Scores score(Set<String> jobSkills, Set<String> workerSkills,
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

    private double skillScore(Set<String> jobSkills, Set<String> workerSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) return 100.0;
        if (workerSkills == null || workerSkills.isEmpty()) return 0.0;
        long matched = jobSkills.stream().filter(workerSkills::contains).count();
        return (double) matched / jobSkills.size() * 100.0;
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
        if (csv == null || csv.isBlank()) return null;
        csv=csv.replace("[","").replace("]","");
        return Arrays.stream(csv.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // Helper method to merge skills from WorkerProfile
    private Set<String> getMergedSkillsSet(String skill1, String skill2, String skill3, String skill4, String skill5) {
        return Stream.of(skill1, skill2, skill3, skill4, skill5)
                .filter(s -> s != null && !s.isEmpty()) // Ignore empty or null skills
                .collect(Collectors.toSet());
    }
}
