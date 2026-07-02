package com.workforcex.backend.service;

import com.workforcex.backend.dto.LoginRequest;
import com.workforcex.backend.dto.RegisterRequest;
import com.workforcex.backend.entity.Role;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private static final String MOBILE = "9876543210";

    @BeforeEach
    void setUp() {
        // no shared stubbing here on purpose — each test sets up exactly what it needs
    }

    @Test
    void register_savesNewUser_whenMobileNumberNotTaken() {
        RegisterRequest request = new RegisterRequest(MOBILE, Role.WORKER,"");

        when(userRepository.existsByMobileNumber(MOBILE)).thenReturn(false);
        when(passwordEncoder.encode(MOBILE)).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        User result = authService.register(request);

        assertThat(result.getMobileNumber()).isEqualTo(MOBILE);
        assertThat(result.getRole()).isEqualTo(Role.WORKER);
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throws_whenMobileNumberAlreadyExists() {
        RegisterRequest request = new RegisterRequest(MOBILE, Role.WORKER,"");

        when(userRepository.existsByMobileNumber(MOBILE)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mobile number already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_succeeds_whenPasswordMatches() {
        LoginRequest request = new LoginRequest(MOBILE, MOBILE);

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setMobileNumber(MOBILE);
        existingUser.setPassword("hashed-password");
        existingUser.setRole(Role.WORKER);

        when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(MOBILE, "hashed-password")).thenReturn(true);

        User result = authService.login(request);

        assertThat(result.getMobileNumber()).isEqualTo(MOBILE);
    }

    @Test
    void login_throws_whenUserNotFound() {
        LoginRequest request = new LoginRequest(MOBILE, MOBILE);

        when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid mobile number or password");
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest(MOBILE, "wrong-password");

        User existingUser = new User();
        existingUser.setMobileNumber(MOBILE);
        existingUser.setPassword("hashed-password");
        existingUser.setRole(Role.WORKER);

        when(userRepository.findByMobileNumber(MOBILE)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid mobile number or password");
    }
}
