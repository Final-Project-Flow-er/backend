package com.chaing.domain.notifications.service.impl;

import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTests {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private final Long userId = 1L;
    private final Long targetId = 100L;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .userId(userId)
                .message("테스트 알림")
                .type(NotificationType.NOTICE)
                .targetId(targetId)
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("SSE 구독")
    void subscribe() {

        // when
        SseEmitter emitter = notificationService.subscribe(userId);

        // then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("전체 알림 전송")
    void sendToAll() {

        // given
        NotificationEvent event = NotificationEvent.forNotice("전체 공지", targetId);

        // when
        notificationService.sendToAll(event);

        // then
        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getUserId().equals(0L) && n.getMessage().equals("전체 공지")
        ));
    }

    @Test
    @DisplayName("단일 유저 알림 전송")
    void sendToUser() {

        // given
        NotificationEvent event = new NotificationEvent(userId, NotificationType.NOTICE, "개인 알림", targetId, false);

        // when
        notificationService.sendToUser(event);

        // then
        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getUserId().equals(userId) && n.getMessage().equals("개인 알림")
        ));
    }

    @Test
    @DisplayName("알림 목록 조회")
    void getNotificationList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        given(notificationRepository.findAllByUserIdInOrderByUpdatedAtDesc(eq(List.of(userId, 0L)), eq(pageable))).willReturn(page);

        // when
        Page<Notification> result = notificationService.getNotificationList(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findAllByUserIdInOrderByUpdatedAtDesc(eq(List.of(userId, 0L)), eq(pageable));
    }

    @Test
    @DisplayName("알림 상세 조회")
    void readNotification() {

        // given
        Long noticeId = 1L;
        given(notificationRepository.findByNotificationIdAndUserId(noticeId, userId)).willReturn(Optional.of(notification));

        // when
        Notification result = notificationService.readNotification(noticeId, userId);

        // then
        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findByNotificationIdAndUserId(noticeId, userId);
    }

    @Test
    @DisplayName("알림 전체 읽음 처리")
    void markAllAsRead() {

        // given
        Notification notification2 = Notification.builder().isRead(false).build();
        given(notificationRepository.findAllByUserIdInAndIsReadFalse(eq(List.of(userId, 0L)))).willReturn(List.of(notification, notification2));

        // when
        notificationService.markAllAsRead(userId);

        // then
        assertThat(notification.isRead()).isTrue();
        assertThat(notification2.isRead()).isTrue();
        verify(notificationRepository).findAllByUserIdInAndIsReadFalse(eq(List.of(userId, 0L)));
    }

    @Test
    @DisplayName("타겟 ID 기반 알림 일괄 삭제")
    void deleteNotificationsByTarget() {

        // when
        notificationService.deleteNotificationsByTarget(NotificationType.NOTICE, targetId);

        // then
        verify(notificationRepository).deleteAllByTypeAndTargetId(NotificationType.NOTICE, targetId);
    }

    @Test
    @DisplayName("단건 알림 삭제")
    void deleteNotification() {

        // given
        Long noticeId = 1L;
        given(notificationRepository.findByNotificationIdAndUserId(noticeId, userId)).willReturn(Optional.of(notification));

        // when
        notificationService.deleteNotification(noticeId, userId);

        // then
        verify(notificationRepository).delete(notification);
    }
}