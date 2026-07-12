package com.workforcex.backend.service;

import com.workforcex.backend.dto.WorkerProfileRequest;
import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.SkillRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    /**
     * Creates the profile if one doesn't exist yet, otherwise updates it.
     * mobileNumber comes from the authenticated JWT, never from the request body.
     */
    public WorkerProfile saveOrUpdate(String mobileNumber, WorkerProfileRequest request) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkerProfile profile = workerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WorkerProfile newProfile = new WorkerProfile();
                    newProfile.setUserId(user.getId());
                    newProfile.setUserMobileNumber(user.getMobileNumber());
                    return newProfile;
                });

        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setEmail(request.email());
        profile.setAddress(request.address());
        profile.setCity(request.city());
        profile.setState(request.state());
        List<String> skills = new ArrayList<>(splitToSet(request.skills()));
        int size = skills.size();
        if (size > 0) profile.setSkill1(skills.get(0));
        if (size > 1) profile.setSkill2(skills.get(1));
        if (size > 2) profile.setSkill3(skills.get(2));
        if (size > 3) profile.setSkill4(skills.get(3));
        if (size > 4) profile.setSkill5(skills.get(4));
        Set<String> allSkillsFromData = new HashSet<>(skills);
        seedSkills(allSkillsFromData);
        profile.setExperience(request.experience());
        profile.setPreferredSalary(request.preferredSalary());
        profile.setDescription(request.description());

        return workerProfileRepository.save(profile);
    }

    public WorkerProfile getByMobileNumber(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return workerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found - complete your profile first"));
    }

    public WorkerProfile getByUserId(UUID userId) {
        return workerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
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