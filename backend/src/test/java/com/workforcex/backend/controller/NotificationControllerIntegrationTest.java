package com.workforcex.backend.controller;

import com.workforcex.backend.entity.Notification;
import com.workforcex.backend.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String MOBILE = "9000033333";

    @Test
    void getUnreadNotifications_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }

    @Test
    void getUnreadNotifications_returnsOnlyUnreadForCurrentUser() throws Exception {
        String token = registerAndLoginAs(MOBILE, "WORKER");
        User user = userRepository.findByCountryCodeAndMobileNumber("+91", MOBILE).orElseThrow();

        Notification unread = new Notification();
        unread.setUser(user);
        unread.setMessage("You have a new match");
        notificationRepository.save(unread);

        Notification read = new Notification();
        read.setUser(user);
        read.setMessage("Old notification");
        read.setRead(true);
        notificationRepository.save(read);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").value("You have a new match"));
    }

    @Test
    void markAsRead_existingNotification_marksItRead() throws Exception {
        String token = registerAndLoginAs(MOBILE, "WORKER");
        User user = userRepository.findByCountryCodeAndMobileNumber("+91", MOBILE).orElseThrow();

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Mark me read");
        notification = notificationRepository.save(notification);

        mockMvc.perform(put("/api/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void markAsRead_unknownNotification_returnsServerErrorHandledByGlobalExceptionHandler() throws Exception {
        String token = registerAndLoginAs(MOBILE, "WORKER");

        mockMvc.perform(put("/api/notifications/" + UUID.randomUUID() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isGreaterThanOrEqualTo(400);
                });
    }
}
