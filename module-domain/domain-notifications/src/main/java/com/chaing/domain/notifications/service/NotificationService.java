package com.chaing.domain.notifications.service;

import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    SseEmitter stream(Long userId);
    void sendToAll(NotificationEvent event);
    void sendToUser(NotificationEvent event) throws IOException;
    void retryableSseSendToAll(NotificationEvent event);
    void deleteNotificationsByTarget(NotificationType type, Long targetId);
    Page<Notification> getNotificationList(Long userId, NotificationType type, Pageable pageable);
    Notification readNotification(Long notificationId, Long userId);
    Map<Long, Boolean> getReadStatusMap(Long userId, List<Long> notificationIds);
    void markAllAsRead(Long userId);
    void updateNotification(NotificationType type, Long targetId, String newMessage);
    void deleteNotification(Long notificationId, Long userId);
    long getUnreadCount(Long userId);
}
