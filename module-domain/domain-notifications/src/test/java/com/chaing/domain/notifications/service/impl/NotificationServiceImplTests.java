package com.chaing.domain.notifications.service.impl;

import com.chaing.domain.notifications.dto.command.NotificationCreateCommand;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    private final Long targetId = 1L;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .userId(userId)
                .message("메시지")
                .isRead(false)
                .type(NotificationType.NOTICE)
                .targetId(targetId)
                .build();
    }

    @Test
    @DisplayName("알림 생성")
    void sendNotification() {

        // given
        NotificationCreateCommand command = NotificationCreateCommand.builder()
                .userId(userId)
                .message("알림 테스트")
                .type(NotificationType.NOTICE)
                .build();

        // when
        notificationService.sendNotification(command, targetId);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("타겟 정보를 기반으로 기존 알림들의 내용을 수정하고 안읽음 처리한다")
    void updateNotificationsByTarget() {

        // given
        notification.read();
        List<Notification> notifications = List.of(notification);
        given(notificationRepository.findAllByTypeAndTargetId(NotificationType.NOTICE, targetId))
                .willReturn(notifications);

        // when
        notificationService.updateNotificationsByTarget(NotificationType.NOTICE, "수정된 메시지", targetId);

        // then
        assertThat(notification.getMessage()).isEqualTo("수정된 메시지");
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("타입과 타겟 ID가 일치하는 알림을 일괄 삭제한다")
    void deleteNotificationsByTarget() {

        // when
        notificationService.deleteNotificationsByTarget(NotificationType.NOTICE, targetId);

        // then
        verify(notificationRepository).deleteAllByTypeAndTargetId(NotificationType.NOTICE, targetId);
    }

    @Test
    @DisplayName("=알림 목록 조회")
    void getNotificationList() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        given(notificationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId, pageable)).willReturn(page);

        // when
        Page<Notification> result = notificationService.getNotificationList(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findAllByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    @Test
    @DisplayName("알림 상세 조회")
    void readNotification() {

        // given
        Long notificationId = 1L;
        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        // when
        Notification result = notificationService.readNotification(notificationId);

        // then
        assertThat(result.isRead()).isTrue();
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("알림 전체 읽음 처리")
    void markAllAsRead() {

        // given
        Notification notification2 = Notification.builder().isRead(false).build();
        given(notificationRepository.findAllByUserIdAndIsReadFalse(userId))
                .willReturn(List.of(notification, notification2));

        // when
        notificationService.markAllAsRead(userId);

        // then
        assertThat(notification.isRead()).isTrue();
        assertThat(notification2.isRead()).isTrue();
    }
}