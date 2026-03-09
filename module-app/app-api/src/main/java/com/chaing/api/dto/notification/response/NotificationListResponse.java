package com.chaing.api.dto.notification.response;

import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import lombok.Builder;

@Builder
public record NotificationListResponse(

        Long notificationId,
        Long userId,
        NotificationType type,
        String message,
        Long targetId,
        boolean isRead
) {
    public static NotificationListResponse of(Notification notification, boolean isRead) {
        return NotificationListResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .targetId(notification.getTargetId())
                .isRead(isRead)
                .build();
    }
}
