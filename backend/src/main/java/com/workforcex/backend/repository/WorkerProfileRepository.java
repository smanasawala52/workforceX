package com.workforcex.backend.repository;

import com.workforcex.backend.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {

    Optional<WorkerProfile> findByUserId(UUID userId);

    List<WorkerProfile> findByCity(String city);

    @Query("SELECT w FROM WorkerProfile w WHERE (:cities IS NULL OR LOWER(w.city) IN :cities) " +
            "AND (:minExperience IS NULL OR w.experience >= :minExperience) " +
            "AND (:maxExperience IS NULL OR w.experience <= :maxExperience) " +
            "AND (:minSalary IS NULL OR w.preferredSalary >= :minSalary) " +
            "AND (:maxSalary IS NULL OR w.preferredSalary <= :maxSalary) " +
            "AND ((:skills IS NULL) OR (LOWER(w.skill1) IN :skills OR LOWER(w.skill2) IN :skills " +
            "OR LOWER(w.skill3) IN :skills OR LOWER(w.skill4) IN :skills OR LOWER(w.skill5) IN :skills))")
    List<WorkerProfile> searchCandidates(
            @Param("cities") Set<String> cities,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("skills") Set<String> skills
    );

    @Query("SELECT w FROM WorkerProfile w " +
            "WHERE (:location IS NULL OR LOWER(w.city) IN :location) " +
            "OR (:experience IS NULL OR w.experience >= :experience) " +
            "OR (:salaryMin IS NULL OR w.preferredSalary >= :salaryMin) " +
            "OR (:salaryMax IS NULL OR w.preferredSalary <= :salaryMax) " +
            "OR ((:skills IS NULL) OR (LOWER(w.skill1) IN :skills OR LOWER(w.skill2) IN :skills " +
            "OR LOWER(w.skill3) IN :skills OR LOWER(w.skill4) IN :skills OR LOWER(w.skill5) IN :skills))")
    List<WorkerProfile> findMatchingWorkers(
            @Param("location") Set<String> location,
            @Param("experience") Integer experience,
            @Param("salaryMin") Double salaryMin,
            @Param("salaryMax") Double salaryMax,
            @Param("skills") Set<String> skills
    );
}
