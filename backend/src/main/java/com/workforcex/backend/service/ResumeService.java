package com.workforcex.backend.service;

import com.workforcex.backend.dto.ResumeParseResponse;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Spiral 2: Resume upload and parsing.
 *
 * Extracts text from PDF using Apache PDFBox.
 * Detects skills using keyword matching against a master skill list.
 * Detects years of experience using regex patterns.
 * Saves extracted data to the worker's profile.
 */
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;

    // Master skill list for blue-collar/semi-skilled workers
    private static final List<String> SKILL_KEYWORDS = List.of(
            "security", "patrolling", "surveillance", "cctv", "access-control",
            "driving", "car", "truck", "bus", "bike", "hcv", "lmv", "delivery",
            "housekeeping", "cleaning", "mopping", "sweeping", "laundry",
            "cooking", "kitchen", "chef", "food-preparation", "south-indian", "north-indian",
            "electrical", "wiring", "electrician", "plumbing", "plumber", "pipe-fitting",
            "carpentry", "woodwork", "furniture", "painting", "masonry", "construction",
            "welding", "fabrication", "fitting", "machinist",
            "ac-repair", "hvac", "refrigeration", "lift", "elevator", "generator",
            "facility-management", "maintenance", "warehouse", "logistics", "inventory",
            "data-entry", "computer", "typing", "ms-office", "tally",
            "reception", "front-desk", "communication", "english",
            "patient-care", "hospital", "nursing", "ward-boy",
            "gardening", "horticulture", "pest-control",
            "forklift", "crane", "excavator", "material-handling",
            "supervision", "team-management", "leadership", "reporting"
    );

    public ResumeParseResponse parseAndSave(String mobileNumber, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }

        // Parse PDF text
        String extractedText;
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(doc);
        }

        // Extract skills
        List<String> detectedSkills = detectSkills(extractedText);
        String skillsCsv = String.join(",", detectedSkills);

        // Detect years of experience
        Integer detectedExp = detectExperience(extractedText);

        // Save to worker profile
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkerProfile profile = workerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    WorkerProfile p = new WorkerProfile();
                    p.setUser(user);
                    return p;
                });

        profile.setResumeFileName(file.getOriginalFilename());
        profile.setResumeText(extractedText.length() > 9000
                ? extractedText.substring(0, 9000) : extractedText);
        profile.setResumeExtractedSkills(skillsCsv);

        // Auto-populate skills if worker hasn't set them yet
        if (profile.getSkills() == null || profile.getSkills().isBlank()) {
            profile.setSkills(skillsCsv);
        }

        // Auto-populate experience if not set
        if (profile.getExperience() == null && detectedExp != null) {
            profile.setExperience(detectedExp);
        }

        workerProfileRepository.save(profile);

        String preview = extractedText.length() > 500
                ? extractedText.substring(0, 500) + "..." : extractedText;

        return new ResumeParseResponse(
                file.getOriginalFilename(),
                skillsCsv,
                detectedSkills,
                detectedExp,
                preview,
                detectedSkills.isEmpty()
                        ? "Resume uploaded. No matching skills detected automatically — please add skills manually."
                        : "Resume uploaded and parsed successfully. " + detectedSkills.size() + " skill(s) detected."
        );
    }

    /**
     * Matches text against the master skill keyword list.
     * Case-insensitive, whole-word matching.
     */
    private List<String> detectSkills(String text) {
        String lowerText = text.toLowerCase();
        return SKILL_KEYWORDS.stream()
                .filter(skill -> {
                    String pattern = skill.replace("-", "[- ]");
                    return Pattern.compile("\\b" + pattern + "\\b")
                            .matcher(lowerText).find();
                })
                .collect(Collectors.toList());
    }

    /**
     * Detects total years of experience from patterns like:
     * "5 years experience", "3+ years", "2 yrs", "10 year"
     */
    private Integer detectExperience(String text) {
        Pattern p = Pattern.compile("(\\d+)\\s*\\+?\\s*(?:years?|yrs?)[\\s,ofexperinc]*experience",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        int maxYears = 0;
        boolean found = false;
        while (m.find()) {
            int years = Integer.parseInt(m.group(1));
            if (years > maxYears && years < 50) { // sanity cap
                maxYears = years;
                found = true;
            }
        }
        return found ? maxYears : null;
    }
}
