package com.chaing.api.facade.notification;

import com.chaing.api.dto.notification.response.NotificationListResponse;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacade {

    private final NotificationService notificationService;

    // SSE 구독 연결
    public SseEmitter subscribe(Long userId) {
        return notificationService.subscribe(userId);
    }

    // 알림 목록 조회
    public Page<NotificationListResponse> getNotificationList(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationService.getNotificationList(userId, pageable);
        return notifications.map(NotificationListResponse::from);
    }

    // 알림 단건 읽음 처리
    @Transactional
    public NotificationListResponse readNotification(Long notificationId, Long userId) {
        Notification notification = notificationService.readNotification(notificationId, userId);
        return NotificationListResponse.from(notification);
    }

    // 알림 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationService.markAllAsRead(userId);
    }

    // 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationService.deleteNotification(notificationId, userId);
    }

    // 알림 일괄 삭제
    @Transactional
    public void deleteNotificationsByTarget(NotificationType type, Long targetId) {
        notificationService.deleteNotificationsByTarget(type, targetId);
    }
}
