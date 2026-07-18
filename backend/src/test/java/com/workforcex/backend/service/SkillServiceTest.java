package com.workforcex.backend.service;

import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    void getAllSkills_returnsAllFromRepository() {
        Skill security = new Skill();
        security.setId(UUID.randomUUID());
        security.setName("security");
        Skill patrolling = new Skill();
        patrolling.setId(UUID.randomUUID());
        patrolling.setName("patrolling");

        when(skillRepository.findAll()).thenReturn(List.of(security, patrolling));

        List<Skill> result = skillService.getAllSkills();

        assertThat(result).containsExactly(security, patrolling);
    }

    @Test
    void getAllSkills_noSkills_returnsEmptyList() {
        when(skillRepository.findAll()).thenReturn(List.of());

        assertThat(skillService.getAllSkills()).isEmpty();
    }
}
