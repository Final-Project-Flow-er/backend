package com.chaing.domain.notifications.service.impl;

import com.chaing.domain.notifications.event.NotificationEvent;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 구독
    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        sendToClient(userId);

        return emitter;
    }

    // 전체 공지사항 처리
    @Override
    public void sendToAll(NotificationEvent event) {
        emitters.forEach((userId, emitter) -> sendSse(emitter, userId, event));

        Notification notification = Notification.builder()
                .userId(0L)
                .type(event.type())
                .message(event.message())
                .targetId(event.targetId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // 단일 유저 알림 처리
    @Override
    public void sendToUser(NotificationEvent event) {
        Notification notification = Notification.builder()
                .userId(event.userId())
                .type(event.type())
                .message(event.message())
                .targetId(event.targetId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(event.userId());
        if (emitter != null) {
            sendSse(emitter, event.userId(), event);
        }
    }

    // SSE 실제 전송 로직 분리
    private void sendSse(SseEmitter emitter, Long userId, NotificationEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(event.targetId()))
                    .name("notification")
                    .data(event.message()));
        } catch (IOException e) {
            emitters.remove(userId);
        }
    }

    // 연결 확인용 더미 데이터 전송 메서드
    private void sendToClient(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("connect").data((Object) "connected!"));
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
    }

    // 해당 타입과 타겟 ID를 가진 알림들을 일괄 삭제
    @Override
    public void deleteNotificationsByTarget(NotificationType type, Long targetId) {
        notificationRepository.deleteAllByTypeAndTargetId(type, targetId);
    }

    // 알림 목록 조회
    @Override
    public Page<Notification> getNotificationList(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserIdInOrderByUpdatedAtDesc(List.of(userId, 0L), pageable);
    }

    // 알림 상세 조회
    @Override
    public Notification readNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.read();
        return notification;
    }

    // 알림 전체 읽음 처리
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadList = notificationRepository.findAllByUserIdInAndIsReadFalse(List.of(userId, 0L));
        unreadList.forEach(Notification::read);
    }

    // 알림 삭제
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notificationRepository.delete(notification);
    }
}
