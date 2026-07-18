package com.workforcex.backend.service;

import com.workforcex.backend.dto.WorkerProfileRequest;
import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.SkillRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileServiceTest {

    @Mock
    private WorkerProfileRepository workerProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private WorkerProfileService workerProfileService;

    private static final String COUNTRY_CODE = "+91";
    private static final String MOBILE = "9876543210";

    private User user() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setCountryCode(COUNTRY_CODE);
        u.setMobileNumber(MOBILE);
        return u;
    }

    @Test
    void saveOrUpdate_createsNewProfile_whenNoneExistsYet() {
        User user = user();
        WorkerProfileRequest request = new WorkerProfileRequest(
                "Asha", "female", LocalDate.of(1995, 5, 1), "asha@example.com",
                "123 Main St", "Saskatoon", "SK", "welding, plumbing, carpentry, painting, driving",
                5, 45000.0, "Experienced tradesperson");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkerProfile result = workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request);

        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.getUserMobileNumber()).isEqualTo(MOBILE);
        assertThat(result.getName()).isEqualTo("Asha");
        assertThat(result.getSkill1()).isEqualTo("welding");
        assertThat(result.getSkill2()).isEqualTo("plumbing");
        assertThat(result.getSkill3()).isEqualTo("carpentry");
        assertThat(result.getSkill4()).isEqualTo("painting");
        assertThat(result.getSkill5()).isEqualTo("driving");
        assertThat(result.getExperience()).isEqualTo(5);
        assertThat(result.getPreferredSalary()).isEqualTo(45000.0);

        ArgumentCaptor<List<Skill>> captor = ArgumentCaptor.forClass(List.class);
        verify(skillRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Skill::getName)
                .containsExactlyInAnyOrder("welding", "plumbing", "carpentry", "painting", "driving");
    }

    @Test
    void saveOrUpdate_updatesExistingProfile_whenOneAlreadyExists() {
        User user = user();
        WorkerProfile existing = new WorkerProfile();
        existing.setUserId(user.getId());
        existing.setName("Old Name");

        WorkerProfileRequest request = new WorkerProfileRequest(
                "New Name", "male", LocalDate.of(1990, 1, 1), "new@example.com",
                "456 Elm St", "Regina", "SK", "cooking", 2, 30000.0, "Chef");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkerProfile result = workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getSkill1()).isEqualTo("cooking");
        assertThat(result.getSkill2()).isNull();
    }

    @Test
    void saveOrUpdate_handlesBlankSkills_withoutSettingAnySkillSlot() {
        User user = user();
        WorkerProfileRequest request = new WorkerProfileRequest(
                "Asha", "female", null, null, null, null, null, "   ", null, null, null);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkerProfile result = workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request);

        assertThat(result.getSkill1()).isNull();
        verify(skillRepository, never()).saveAll(any());
    }

    @Test
    void saveOrUpdate_skipsAlreadySeededSkills() {
        User user = user();
        WorkerProfileRequest request = new WorkerProfileRequest(
                "Asha", null, null, null, null, null, null, "welding, plumbing", null, null, null);

        Skill existingSkill = new Skill();
        existingSkill.setName("welding");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of(existingSkill));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request);

        ArgumentCaptor<List<Skill>> captor = ArgumentCaptor.forClass(List.class);
        verify(skillRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Skill::getName).containsExactly("plumbing");
    }

    @Test
    void saveOrUpdate_seedSkillsSwallowsRepositoryException() {
        User user = user();
        WorkerProfileRequest request = new WorkerProfileRequest(
                "Asha", null, null, null, null, null, null, "welding", null, null, null);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenThrow(new RuntimeException("db down"));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkerProfile result = workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request);

        assertThat(result.getSkill1()).isEqualTo("welding");
    }

    @Test
    void saveOrUpdate_throws_whenUserNotFound() {
        WorkerProfileRequest request = new WorkerProfileRequest(
                "Asha", null, null, null, null, null, null, null, null, null, null);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerProfileService.saveOrUpdate(COUNTRY_CODE, MOBILE, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getByMobileNumber_returnsProfile_whenExists() {
        User user = user();
        WorkerProfile profile = new WorkerProfile();
        profile.setUserId(user.getId());

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        WorkerProfile result = workerProfileService.getByMobileNumber(COUNTRY_CODE, MOBILE);

        assertThat(result).isSameAs(profile);
    }

    @Test
    void getByMobileNumber_throws_whenProfileMissing() {
        User user = user();
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerProfileService.getByMobileNumber(COUNTRY_CODE, MOBILE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("complete your profile first");
    }

    @Test
    void getByMobileNumber_throws_whenUserMissing() {
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerProfileService.getByMobileNumber(COUNTRY_CODE, MOBILE))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getByUserId_returnsProfile_whenExists() {
        UUID userId = UUID.randomUUID();
        WorkerProfile profile = new WorkerProfile();
        profile.setUserId(userId);

        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        WorkerProfile result = workerProfileService.getByUserId(userId);

        assertThat(result).isSameAs(profile);
    }

    @Test
    void getByUserId_throws_whenMissing() {
        UUID userId = UUID.randomUUID();
        when(workerProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerProfileService.getByUserId(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Worker profile not found");
    }
}
