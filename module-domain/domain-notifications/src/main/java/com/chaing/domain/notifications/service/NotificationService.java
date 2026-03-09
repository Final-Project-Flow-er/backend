package com.chaing.domain.notifications.service;

import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {

    SseEmitter stream(Long userId);
    void sendToAll(NotificationEvent event);
    void sendToUser(NotificationEvent event);
    void deleteNotificationsByTarget(NotificationType type, Long targetId);
    Page<Notification> getNotificationList(Long userId, Pageable pageable);
    Notification readNotification(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    void updateNotification(Long notificationId, String newMessage);
    void deleteNotification(Long notificationId, Long userId);
}
