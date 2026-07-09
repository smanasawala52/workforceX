package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobRequest;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private EmployerProfileRepository employerProfileRepository;

    public Job createJob(String employerMobileNumber, JobRequest request) {
        User employer = userRepository.findByMobileNumber(employerMobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Job job = new Job();
        job.setEmployerMobileNumber(employer.getMobileNumber());
        String companyName = request.companyName();
        if (companyName != null && !companyName.isBlank()) {
            job.setCompanyName(companyName);
        } else {
            EmployerProfile profile = employerProfileRepository.findByUserId(employer.getId())
                    .orElseGet(() -> {
                        EmployerProfile p = new EmployerProfile();
                        p.setUser(employer);
                        return p;
                    });
            if (companyName != null && !companyName.isBlank()) {
                job.setCompanyName(companyName);
            } else if (profile.getContactPerson() != null && !profile.getContactPerson().isBlank()) {
                job.setCompanyName(profile.getContactPerson());
            } else {
                job.setCompanyName("Unknown Company");
            }
        }
        job.setEmployerId(employer.getId());
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
        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!employer.getMobileNumber().equals(employerMobileNumber)) {
            throw new IllegalArgumentException("You do not have permission to access this job");
        }
        return job;
    }

    private void applyRequest(Job job, JobRequest request) {
        job.setTitle(request.title());
        List<String> skills = new ArrayList<>(splitToSet(request.skillsRequired()));
        int size = skills.size();
        if (size > 0) job.setSkillsRequired1(skills.get(0));
        if (size > 1) job.setSkillsRequired2(skills.get(1));
        if (size > 2) job.setSkillsRequired3(skills.get(2));
        if (size > 3) job.setSkillsRequired4(skills.get(3));
        if (size > 4) job.setSkillsRequired5(skills.get(4));
        Set<String> allSkillsFromData = new HashSet<>(skills);
        seedSkills(allSkillsFromData);
        job.setExperienceRequired(request.experienceRequired());
        job.setLocation(request.location());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setOpenPositions(request.openPositions());
        job.setDescription(request.description());
    }

    private Set<String> splitToSet(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return Arrays.stream(csv.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
    private void seedSkills(Set<String> allSkillsFromData) {
        try {
            // 1. Get all names currently in the DB
            List<String> existingSkills = skillRepository.findAll()
                    .stream()
                    .map(Skill::getName)
                    .toList();

            // 2. Filter out skills that already exist
            List<Skill> newSkills = allSkillsFromData.stream()
                    .filter(name -> !existingSkills.contains(name))
                    .map(name -> {
                        Skill skill = new Skill();
                        skill.setName(name);
                        return skill;
                    })
                    .collect(Collectors.toList());

            // 3. Batch save all new skills at once
            if (!newSkills.isEmpty()) {
                skillRepository.saveAll(newSkills);
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
