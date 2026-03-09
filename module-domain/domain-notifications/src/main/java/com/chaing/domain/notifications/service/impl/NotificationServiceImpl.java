package com.chaing.domain.notifications.service.impl;

import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.entity.NotificationStatus;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.notifications.exception.NotificationErrorCode;
import com.chaing.domain.notifications.exception.NotificationException;
import com.chaing.domain.notifications.repository.NotificationRepository;
import com.chaing.domain.notifications.repository.NotificationStatusRepository;
import com.chaing.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationStatusRepository notificationStatusRepository;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectProvider<NotificationService> selfProvider;

    // SSE 스트림
    @Override
    public SseEmitter stream(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        SseEmitter previous = emitters.put(userId, emitter);

        if (previous != null) previous.complete();

        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> emitters.remove(userId, emitter));
        emitter.onError((e) -> emitters.remove(userId, emitter));

        sendToClient(userId);

        return emitter;
    }

    // 전체 공지사항 처리
    @Override
    public void sendToAll(NotificationEvent event) {
        Notification notification = Notification.builder()
                .userId(0L)
                .type(event.type())
                .message(event.message())
                .targetId(event.targetId())
                .build();
        notificationRepository.save(notification);
        Objects.requireNonNull(selfProvider.getIfAvailable()).retryableSseSendToAll(event);
    }

    // 재시도 전용 메서드
    @Retryable(retryFor = { IOException.class, Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Override
    public void retryableSseSendToAll(NotificationEvent event) {
        emitters.forEach((userId, emitter) -> {
            try {
                sendSse(emitter, event);
            } catch (IOException e) {
                emitters.remove(userId, emitter);
                throw new NotificationException(NotificationErrorCode.SSE_SEND_FAIL);
            }
        });
    }

    // 단일 유저 알림 처리
    @Override
    public void sendToUser(NotificationEvent event) throws IOException {
        Notification notification = Notification.builder()
                .userId(event.userId())
                .type(event.type())
                .message(event.message())
                .targetId(event.targetId())
                .build();
        notificationRepository.save(notification);

        SseEmitter emitter = emitters.get(event.userId());
        if (emitter != null) {
            sendSse(emitter, event);
        }
    }

    // SSE 실제 전송 로직
    private void sendSse(SseEmitter emitter, NotificationEvent event) throws IOException {
        emitter.send(SseEmitter.event()
                .id(String.valueOf(event.targetId()))
                .name("notification")
                .data(event.message()));
    }

    // 연결 확인용 더미 데이터 전송 메서드
    private void sendToClient(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("connect").data((Object) "connected!"));
            } catch (IOException e) {
                emitters.remove(userId, emitter);
            }
        }
    }

    // 해당 타입과 타겟 ID를 가진 알림들을 일괄 삭제
    @Override
    public void deleteNotificationsByTarget(NotificationType type, Long targetId) {
        notificationRepository.deleteAllByTypeAndTargetId(type, targetId);

        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("refresh").data("refresh"));
            } catch (IOException e) {
                emitters.remove(userId, emitter);
            }
        });
    }

    // 알림 목록 조회
    @Override
    public Page<Notification> getNotificationList(Long userId, NotificationType type, Pageable pageable) {
        return notificationRepository.findAllMyNotificationsByType(userId, type, pageable);
    }

    // 알림 상세 조회
    @Override
    public Notification readNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserIdOrAll(notificationId, userId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        NotificationStatus status = notificationStatusRepository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseGet(() -> NotificationStatus.builder().userId(userId).notificationId(notificationId).isRead(false).build());

        status.read();
        notificationStatusRepository.save(status);
        return notification;
    }

    // 조회 상태 매핑
    @Override
    public Map<Long, Boolean> getReadStatusMap(Long userId, List<Long> notificationIds) {
        Map<Long, Boolean> readStatusMap = notificationIds.stream()
                .collect(Collectors.toMap(id -> id, id -> false));

        List<NotificationStatus> statuses = notificationStatusRepository
                .findAllByUserIdAndNotificationIdIn(userId, notificationIds);

        statuses.forEach(status -> readStatusMap.put(status.getNotificationId(), status.isRead()));

        return readStatusMap;
    }

    // 알림 전체 읽음 처리
    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findAllUnreadNotificationsList(userId);

        for (Notification n : unreadNotifications) {
            NotificationStatus status = notificationStatusRepository.findByUserIdAndNotificationId(userId, n.getNotificationId())
                    .orElseGet(() -> NotificationStatus.builder()
                            .userId(userId)
                            .notificationId(n.getNotificationId())
                            .isRead(false)
                            .build());
            status.read();
            notificationStatusRepository.save(status);
        }
    }

    // 미읽음 알림 수 조회
    @Override
    public Map<String, Map<String, Long>> getUnreadCount(Long userId) {
        Map<String, Map<String, Long>> counts = new java.util.HashMap<>();

        Map<String, Long> allCounts = new java.util.HashMap<>();
        allCounts.put("unread", notificationRepository.countUnreadByType(userId, null));
        allCounts.put("total", notificationRepository.countTotalByType(userId, null));
        counts.put("all", allCounts);

        for (NotificationType type : NotificationType.values()) {
            Map<String, Long> typeCounts = new java.util.HashMap<>();
            typeCounts.put("unread", notificationRepository.countUnreadByType(userId, type));
            typeCounts.put("total", notificationRepository.countTotalByType(userId, type));
            counts.put(type.name(), typeCounts);
        }

        return counts;
    }

    // 알림 수정
    @Override
    public void updateNotification(NotificationType type, Long targetId, String newMessage) {
        notificationRepository.findByTypeAndTargetId(type, targetId).ifPresent(notification -> {
            notificationStatusRepository.deleteAllByNotificationId(notification.getNotificationId());
            notificationRepository.delete(notification);
        });

        NotificationEvent event = NotificationEvent.ofAll(type, newMessage, targetId);
        sendToAll(event);
    }

    // 알림 삭제
    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserIdOrAll(notificationId, userId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getUserId() != 0) {
            notificationStatusRepository.deleteByUserIdAndNotificationId(userId, notificationId);
            notificationRepository.delete(notification);
        } else {
            NotificationStatus status = notificationStatusRepository.findByUserIdAndNotificationId(userId, notificationId)
                    .orElseGet(() -> NotificationStatus.builder().userId(userId).notificationId(notificationId).isRead(true).build());

            status.delete();
            notificationStatusRepository.save(status);
        }
    }
}
