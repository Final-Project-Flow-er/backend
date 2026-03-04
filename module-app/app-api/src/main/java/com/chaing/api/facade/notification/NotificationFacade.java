package com.chaing.api.facade.notification;

import com.chaing.api.dto.notification.response.NotificationListResponse;
import com.chaing.domain.notifications.dto.command.NotificationCreateCommand;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.service.NotificationService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationFacade {

    private final NotificationService notificationService;
    private final UserManagementService userManagementService;

    // 전체 알림 생성
    @Transactional
    public void sendNotificationToAll(NotificationType type, String message, Long targetId) {

        List<Long> allUserIds = userManagementService.getAllUserIds();

        allUserIds.forEach(receiverId -> {
            NotificationCreateCommand command = NotificationCreateCommand.builder()
                    .userId(receiverId)
                    .type(type)
                    .message(message)
                    .targetId(targetId)
                    .build();

            notificationService.sendNotification(command);
        });
    }

    // 특정 회원에게 알림 생성
    @Transactional
    public void sendNotificationToUser(Long userId, NotificationType type, String message, Long targetId) {

        NotificationCreateCommand command = NotificationCreateCommand.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .targetId(targetId)
                .build();

        notificationService.sendNotification(command);
    }

    // 알림 수정
    @Transactional
    public void updateNotificationsByTarget(NotificationType type, String message, Long targetId) {
        notificationService.updateNotificationsByTarget(type, message, targetId);
    }

    // 알림 일괄 삭제
    @Transactional
    public void deleteNotificationsByTarget(NotificationType type, Long targetId) {
        notificationService.deleteNotificationsByTarget(type, targetId);
    }

    // 알림 목록 조회
    public Page<NotificationListResponse> getNotificationList(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationService.getNotificationList(userId, pageable);
        return notifications.map(NotificationListResponse::from);
    }

    // 알림 단건 읽음 처리
    @Transactional
    public NotificationListResponse readNotification(Long notificationId) {
        Notification notification = notificationService.readNotification(notificationId);
        return NotificationListResponse.from(notification);
    }

    // 알림 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationService.markAllAsRead(userId);
    }

    // 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationService.deleteNotification(notificationId);
    }
}
