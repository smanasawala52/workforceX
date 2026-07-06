package com.workforcex.backend.dto;

import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.entity.JobApplication;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationResponse(
        UUID applicationId,
        UUID jobId,
        String jobTitle,
        String companyName,
        UUID workerId,
        String workerName,
        String workerMobile,
        String workerSkills,
        Integer workerExperience,
        String workerCity,
        Double workerPreferredSalary,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        String employerMobile
) {
    public static JobApplicationResponse fromEntity(JobApplication app, String companyName) {
        var worker = app.getWorker();
        var job    = app.getJob();
        return new JobApplicationResponse(
                app.getId(),
                job.getId(),
                job.getTitle(),
                companyName,
                worker.getId(),
                null, null, null, null, null, null, // worker profile fields filled below
                app.getStatus(),
                app.getAppliedAt(),
                app.getUpdatedAt(),
                job.getEmployer().getMobileNumber()
        );
    }

    /** Full version that includes worker profile data */
    public static JobApplicationResponse fromEntityWithProfile(
            JobApplication app,
            String companyName,
            String workerName,
            String workerSkills,
            Integer workerExp,
            String workerCity,
            Double workerSalary
    ) {
        var worker = app.getWorker();
        var job    = app.getJob();
        return new JobApplicationResponse(
                app.getId(),
                job.getId(),
                job.getTitle(),
                companyName,
                worker.getId(),
                workerName,
                worker.getMobileNumber(),
                workerSkills,
                workerExp,
                workerCity,
                workerSalary,
                app.getStatus(),
                app.getAppliedAt(),
                app.getUpdatedAt(),
                job.getEmployer().getMobileNumber()
        );
    }
}
