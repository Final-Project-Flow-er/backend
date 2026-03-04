package com.chaing.domain.notifications.service;

import com.chaing.domain.notifications.dto.command.NotificationCreateCommand;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void sendNotification(NotificationCreateCommand command);
    void updateNotificationsByTarget(NotificationType type, String message, Long targetId);
    void deleteNotificationsByTarget(NotificationType type, Long targetId);
    Page<Notification> getNotificationList(Long userId, Pageable pageable);
    Notification readNotification(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);
}
