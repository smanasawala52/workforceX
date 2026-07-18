package com.workforcex.backend.service;

import com.workforcex.backend.entity.Notification;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        String jobTitle = "Test Job";

        notificationService.createNotification(user, message, linkType, linkId, jobTitle);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadNotifications_shouldCallRepositoryWithCorrectUserId() {
        UUID userId = UUID.randomUUID();
        notificationService.getUnreadNotifications(userId);
        verify(notificationRepository).findByUserIdAndIsReadFalse(userId);
    }

    @Test
    void markAsRead_existingNotification_setsReadTrueAndSaves() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setRead(false);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(notificationId);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_notificationNotFound_throwsIllegalArgumentException() {
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(notificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification not found");
    }
}
