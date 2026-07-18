package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobRequest;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.SkillRepository;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @InjectMocks
    private JobService jobService;

    private static final String COUNTRY_CODE = "+91";
    private static final String MOBILE = "9876543210";
    private static final String FULL_MOBILE = COUNTRY_CODE + MOBILE;

    private User employer() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setCountryCode(COUNTRY_CODE);
        u.setMobileNumber(MOBILE);
        return u;
    }

    private JobRequest request(String companyName, String skills) {
        return new JobRequest("Security Guard", skills, 2, "Saskatoon", 30000.0, 45000.0, 3, "desc", companyName);
    }

    @Test
    void createJob_usesRequestCompanyName_whenProvided() {
        User employer = employer();
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(FULL_MOBILE, request("Acme Corp", "welding,plumbing"));

        assertThat(result.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(result.getEmployerId()).isEqualTo(employer.getId());
        assertThat(result.getEmployerMobileNumber()).isEqualTo(FULL_MOBILE);
        assertThat(result.getSkillsRequired1()).isEqualTo("welding");
        assertThat(result.getSkillsRequired2()).isEqualTo("plumbing");
        verify(employerProfileRepository, never()).findByUserId(any());
    }

    @Test
    void createJob_fallsBackToContactPerson_whenCompanyNameBlankAndProfileHasContactPerson() {
        User employer = employer();
        EmployerProfile profile = new EmployerProfile();
        profile.setContactPerson("Jane Doe");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(employerProfileRepository.findByUserId(employer.getId())).thenReturn(Optional.of(profile));
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(FULL_MOBILE, request("", null));

        assertThat(result.getCompanyName()).isEqualTo("Jane Doe");
        assertThat(result.getSkillsRequired1()).isNull();
    }

    @Test
    void createJob_fallsBackToUnknownCompany_whenNoCompanyNameOrContactPerson() {
        User employer = employer();
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(employerProfileRepository.findByUserId(employer.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(FULL_MOBILE, request(null, null));

        assertThat(result.getCompanyName()).isEqualTo("Unknown Company");
    }

    @Test
    void createJob_seedsOnlyNewSkills() {
        User employer = employer();
        Skill existing = new Skill();
        existing.setName("welding");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(skillRepository.findAll()).thenReturn(List.of(existing));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        jobService.createJob(FULL_MOBILE, request("Acme", "welding,plumbing"));

        ArgumentCaptor<List<Skill>> captor = ArgumentCaptor.forClass(List.class);
        verify(skillRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Skill::getName).containsExactly("plumbing");
    }

    @Test
    void createJob_seedSkillsSwallowsRepositoryException() {
        User employer = employer();
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(skillRepository.findAll()).thenThrow(new RuntimeException("db down"));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob(FULL_MOBILE, request("Acme", "welding"));

        assertThat(result.getSkillsRequired1()).isEqualTo("welding");
    }

    @Test
    void createJob_throws_whenEmployerNotFound() {
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.createJob(FULL_MOBILE, request("Acme", null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createJob_recognizesUaeMobileNumber() {
        User employer = employer();
        employer.setCountryCode("+971");
        when(userRepository.findByCountryCodeAndMobileNumber("+971", "501234567")).thenReturn(Optional.of(employer));
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.createJob("+971501234567", request("Acme", null));

        assertThat(result.getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void updateJob_updatesFieldsOnOwnedJob() {
        User employer = employer();
        UUID jobId = UUID.randomUUID();
        Job existingJob = new Job();
        existingJob.setId(jobId);
        existingJob.setEmployerId(employer.getId());
        existingJob.setTitle("Old Title");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        Job result = jobService.updateJob(FULL_MOBILE, jobId, request("Acme", "cooking"));

        assertThat(result.getTitle()).isEqualTo("Security Guard");
        assertThat(result.getSkillsRequired1()).isEqualTo("cooking");
    }

    @Test
    void updateJob_throws_whenJobNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.updateJob(FULL_MOBILE, jobId, request("Acme", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Job not found");
    }

    @Test
    void updateJob_throws_whenOwningUserNotFound() {
        UUID jobId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setEmployerId(ownerId);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.updateJob(FULL_MOBILE, jobId, request("Acme", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateJob_throws_whenCallerIsNotJobOwner() {
        User owner = employer();
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setEmployerId(owner.getId());

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> jobService.updateJob("+919999999999", jobId, request("Acme", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void deleteJob_deletesOwnedJob() {
        User employer = employer();
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setEmployerId(employer.getId());

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        jobService.deleteJob(FULL_MOBILE, jobId);

        verify(jobRepository).delete(job);
    }

    @Test
    void getJobById_returnsOwnedJob() {
        User employer = employer();
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setEmployerId(employer.getId());

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));

        Job result = jobService.getJobById(FULL_MOBILE, jobId);

        assertThat(result).isSameAs(job);
    }

    @Test
    void getJobsForEmployer_returnsAllJobsForEmployer() {
        User employer = employer();
        Job job1 = new Job();
        Job job2 = new Job();

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(employer));
        when(jobRepository.findAllByEmployerId(employer.getId())).thenReturn(List.of(job1, job2));

        List<Job> result = jobService.getJobsForEmployer(FULL_MOBILE);

        assertThat(result).containsExactly(job1, job2);
    }
}
