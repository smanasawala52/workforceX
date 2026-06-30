package com.workforcex.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test: real HTTP request -> controller -> service -> H2 database.
 * Confirms registration and login actually work end-to-end, not just in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String MOBILE = "9111122233";

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void register_returns201_withUserDetails_noPasswordExposed() throws Exception {
        String body = """
                { "mobileNumber": "%s", "role": "WORKER" }
                """.formatted(MOBILE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mobileNumber").value(MOBILE))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_returns400_whenMobileNumberInvalid() throws Exception {
        String body = """
                { "mobileNumber": "123", "role": "WORKER" }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns400_whenMobileNumberAlreadyRegistered() throws Exception {
        String body = """
                { "mobileNumber": "%s", "role": "WORKER" }
                """.formatted(MOBILE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mobile number already registered"));
    }

    @Test
    void login_returns200_withCorrectCredentials() throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "EMPLOYER" }
                """.formatted(MOBILE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody));

        // Dev-mode rule: password equals mobile number
        String loginBody = """
                { "mobileNumber": "%s", "password": "%s" }
                """.formatted(MOBILE, MOBILE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobileNumber").value(MOBILE))
                .andExpect(jsonPath("$.role").value("EMPLOYER"));
    }

    @Test
    void login_returns400_withWrongPassword() throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "WORKER" }
                """.formatted(MOBILE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody));

        String loginBody = """
                { "mobileNumber": "%s", "password": "wrong-password" }
                """.formatted(MOBILE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns400_whenUserDoesNotExist() throws Exception {
        String loginBody = """
                { "mobileNumber": "9999999999", "password": "9999999999" }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isBadRequest());
    }
}
