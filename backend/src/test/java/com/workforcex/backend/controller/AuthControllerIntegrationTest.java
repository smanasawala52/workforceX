package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String MOBILE = "9111122233";

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
        registerAndLoginAs(MOBILE, "EMPLOYER"); // register + login in one shot via helper
        // re-login explicitly to assert response shape
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mobileNumber": "%s", "password": "%s" }
                                """.formatted(MOBILE, MOBILE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobileNumber").value(MOBILE))
                .andExpect(jsonPath("$.role").value("EMPLOYER"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_returns400_withWrongPassword() throws Exception {
        registerAndLoginAs(MOBILE, "WORKER");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mobileNumber": "%s", "password": "wrong" }
                                """.formatted(MOBILE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns400_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mobileNumber": "9999999999", "password": "9999999999" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_rejectsRequest_withoutToken() throws Exception {
        mockMvc.perform(post("/api/some-protected-endpoint"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }
}
