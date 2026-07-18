package com.workforcex.backend.controller;

import com.workforcex.backend.entity.Skill;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SkillControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void getAllSkills_noSkillsSeeded_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllSkills_withSeededSkills_returnsThem() throws Exception {
        Skill security = new Skill();
        security.setName("security");
        Skill patrolling = new Skill();
        patrolling.setName("patrolling");
        skillRepository.save(security);
        skillRepository.save(patrolling);

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name")
                        .value(org.hamcrest.Matchers.containsInAnyOrder("security", "patrolling")));
    }

    @Test
    void getAllSkills_doesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk());
    }
}
