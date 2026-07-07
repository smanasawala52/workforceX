package com.workforcex.backend.service;

import com.workforcex.backend.entity.Notification;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationService notificationService;

    @Test
    void createNotification_shouldSaveNotificationWithMessage() {
        User user = new User();
        String message = "Test message";

        notificationService.createNotification(user, message);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_shouldSaveNotificationWithLinkData() {
        User user = new User();
        String message = "Test message with link";
        String linkType = "TEST_LINK";
        UUID linkId = UUID.randomUUID();

        notificationService.createNotification(user, message, linkType, linkId);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_shouldCallRepositoryWithCorrectUserId() {
        UUID userId = UUID.randomUUID();
        notificationService.getUnreadNotifications(userId);
        verify(notificationRepository).findByUserIdAndIsReadFalse(userId);
    }
}
