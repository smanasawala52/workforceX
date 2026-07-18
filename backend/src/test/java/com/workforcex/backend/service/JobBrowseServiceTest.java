package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobBrowseResponse;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.repository.JobRepository;
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
class JobBrowseServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobBrowseService jobBrowseService;

    @Test
    void getAllJobs_mapsAllJobsToResponses() {
        Job job1 = new Job();
        job1.setId(UUID.randomUUID());
        job1.setTitle("Electrician");
        job1.setCompanyName("Acme Corp");
        job1.setSkillsRequired1("wiring");

        Job job2 = new Job();
        job2.setId(UUID.randomUUID());
        job2.setTitle("Plumber");
        job2.setCompanyName(null);

        when(jobRepository.findAllWithEmployerAndProfile()).thenReturn(List.of(job1, job2));

        List<JobBrowseResponse> result = jobBrowseService.getAllJobs();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Electrician");
        assertThat(result.get(0).companyName()).isEqualTo("Acme Corp");
        assertThat(result.get(0).skillsRequired()).isEqualTo("wiring");
        assertThat(result.get(1).companyName()).isEqualTo("Unknown Company");
    }

    @Test
    void getAllJobs_returnsEmptyList_whenNoJobs() {
        when(jobRepository.findAllWithEmployerAndProfile()).thenReturn(List.of());

        List<JobBrowseResponse> result = jobBrowseService.getAllJobs();

        assertThat(result).isEmpty();
    }
}
