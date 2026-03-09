package com.chaing.api.dto.notification.response;

import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import lombok.Builder;

@Builder
public record NotificationListResponse(

        Long userId,
        NotificationType type,
        String message,
        Long targetId
) {
    public static NotificationListResponse from(Notification notification) {
        return new NotificationListResponse(
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.getTargetId()
        );
    }
}
