package com.workforcex.backend.repository;

import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

}
