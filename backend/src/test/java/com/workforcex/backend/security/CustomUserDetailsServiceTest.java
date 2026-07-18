package com.workforcex.backend.security;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setCountryCode("+91");
        user.setMobileNumber("9876543210");
        user.setPassword("hashed-password");
        user.setRole(Role.WORKER);
    }

    @Test
    void loadUserByUsername_existingIndiaUser_returnsUserDetailsWithRolePrefix() {
        when(userRepository.findByCountryCodeAndMobileNumber(eq("+91"), eq("9876543210")))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("+919876543210");

        assertThat(details.getUsername()).isEqualTo("+919876543210");
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_WORKER");
    }

    @Test
    void loadUserByUsername_existingUaeUser_splitsCorrectly() {
        user.setCountryCode("+971");
        user.setMobileNumber("501234567");
        user.setRole(Role.EMPLOYER);
        when(userRepository.findByCountryCodeAndMobileNumber(eq("+971"), eq("501234567")))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("+971501234567");

        assertThat(details.getUsername()).isEqualTo("+971501234567");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_EMPLOYER");
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByCountryCodeAndMobileNumber(eq("+91"), eq("9876543210")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("+919876543210"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("+919876543210");
    }
}
