package com.workforcex.backend.service;

import com.workforcex.backend.dto.EmployerProfileRequest;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerProfileServiceTest {

    @Mock
    private EmployerProfileRepository employerProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployerProfileService employerProfileService;

    private static final String COUNTRY_CODE = "+91";
    private static final String MOBILE = "9876543210";
    private static final String FULL_MOBILE = COUNTRY_CODE + MOBILE;

    private User user() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setCountryCode(COUNTRY_CODE);
        u.setMobileNumber(MOBILE);
        return u;
    }

    @Test
    void saveOrUpdate_createsNewProfile_whenNoneExists() {
        User user = user();
        EmployerProfileRequest request = new EmployerProfileRequest("Acme Corp", "Jane Doe", "jane@acme.com", "123 Main St");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployerProfile result = employerProfileService.saveOrUpdate(FULL_MOBILE, request);

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(result.getContactPerson()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@acme.com");
        assertThat(result.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void saveOrUpdate_updatesExistingProfile() {
        User user = user();
        EmployerProfile existing = new EmployerProfile();
        existing.setUser(user);
        existing.setCompanyName("Old Corp");

        EmployerProfileRequest request = new EmployerProfileRequest("New Corp", "John Doe", "john@new.com", "456 Elm St");

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployerProfile result = employerProfileService.saveOrUpdate(FULL_MOBILE, request);

        assertThat(result.getCompanyName()).isEqualTo("New Corp");
    }

    @Test
    void saveOrUpdate_throws_whenUserNotFound() {
        EmployerProfileRequest request = new EmployerProfileRequest("Acme", "Jane", "j@a.com", "addr");
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerProfileService.saveOrUpdate(FULL_MOBILE, request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void saveOrUpdate_recognizesUaeMobileNumber() {
        User user = user();
        user.setCountryCode("+971");
        EmployerProfileRequest request = new EmployerProfileRequest("Acme", "Jane", "j@a.com", "addr");

        when(userRepository.findByCountryCodeAndMobileNumber("+971", "501234567")).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployerProfile result = employerProfileService.saveOrUpdate("+971501234567", request);

        assertThat(result.getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void saveOrUpdate_fallsBackToIndia_whenUnrecognizedPrefix() {
        User user = user();
        user.setCountryCode("+91");
        EmployerProfileRequest request = new EmployerProfileRequest("Acme", "Jane", "j@a.com", "addr");

        when(userRepository.findByCountryCodeAndMobileNumber("+91", "12345")).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployerProfile result = employerProfileService.saveOrUpdate("12345", request);

        assertThat(result.getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void getByMobileNumber_returnsProfile_whenExists() {
        User user = user();
        EmployerProfile profile = new EmployerProfile();
        profile.setUser(user);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(profile));

        EmployerProfile result = employerProfileService.getByMobileNumber(FULL_MOBILE);

        assertThat(result).isSameAs(profile);
    }

    @Test
    void getByMobileNumber_throws_whenProfileMissing() {
        User user = user();
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerProfileService.getByMobileNumber(FULL_MOBILE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("complete your profile first");
    }
}
