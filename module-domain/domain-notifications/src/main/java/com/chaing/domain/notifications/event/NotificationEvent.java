package com.chaing.domain.notifications.event;

import com.chaing.domain.notifications.enums.NotificationType;

public record NotificationEvent(

        Long userId,
        NotificationType type,
        String message,
        Long targetId,
        boolean isAll
) {
    public static NotificationEvent forNotice(String message, Long noticeId) {
        return new NotificationEvent(
                null,
                NotificationType.NOTICE,
                message,
                noticeId,
                true);
    }
}
