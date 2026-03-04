package com.chaing.domain.notifications.service.impl;

import com.chaing.domain.notifications.dto.command.NotificationCreateCommand;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.exception.NotificationErrorCode;
import com.chaing.domain.notifications.exception.NotificationException;
import com.chaing.domain.notifications.repository.NotificationRepository;
import com.chaing.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 생성
    @Override
    public void sendNotification(NotificationCreateCommand command, Long targetId) {
        Notification notification = Notification.createNotification(command, targetId);
        notificationRepository.save(notification);
    }

    // 기존 알림 업데이트
    @Override
    public void updateNotificationsByTarget(NotificationType type, String message, Long targetId) {
        List<Notification> notifications = notificationRepository.findAllByTypeAndTargetId(type, targetId);
        notifications.forEach(notification -> {
            notification.updateContent(message);
            notification.markAsUnread();
        });
    }

    // 해당 타입과 타겟 ID를 가진 알림들을 일괄 삭제
    @Override
    public void deleteNotificationsByTarget(NotificationType type, Long targetId) {
        notificationRepository.deleteAllByTypeAndTargetId(type, targetId);
    }

    // 알림 목록 조회
    @Override
    public Page<Notification> getNotificationList(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    // 알림 상세 조회
    @Override
    public Notification readNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
        return notification;
    }

    // 알림 전체 읽음 처리
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadList = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        unreadList.forEach(Notification::read);
    }

    // 알림 삭제
    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.delete();
    }
}
