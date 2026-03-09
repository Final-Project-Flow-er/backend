package com.chaing.domain.notifications.event;

import com.chaing.domain.notifications.enums.NotificationType;

public record NotificationEvent(

        Long userId,
        NotificationType type,
        String message,
        Long targetId,
        boolean isAll,
        boolean isUpdate
) {
    // 공지사항 등록
    public static NotificationEvent ofAll(NotificationType type, String message, Long targetId) {
        return new NotificationEvent(null, type, message, targetId, true, false);
    }

    // 공지사항 수정
    public static NotificationEvent ofUpdate(NotificationType type, String message, Long targetId) {
        return new NotificationEvent(null, type, message, targetId, true, true);
    }
}
