package com.workforcex.backend.service;

import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setCountryCode("+91");
        user.setMobileNumber("9876543210");
        user.setPassword("hash");
        user.setRole(Role.WORKER);
    }

    @Test
    void getUserByMobile_indiaNumber_looksUpBySplitCountryCode() {
        when(userRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByMobile("+919876543210");

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserByMobile_uaeNumber_looksUpBySplitCountryCode() {
        user.setCountryCode("+971");
        user.setMobileNumber("501234567");
        when(userRepository.findByCountryCodeAndMobileNumber("+971", "501234567"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByMobile("+971501234567");

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserByMobile_unknownPrefix_fallsBackToIndiaCode() {
        when(userRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.of(user));

        User result = userService.getUserByMobile("9876543210");

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserByMobile_notFound_throwsNotFoundResponseStatusException() {
        when(userRepository.findByCountryCodeAndMobileNumber("+91", "9876543210"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByMobile("+919876543210"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }
}
