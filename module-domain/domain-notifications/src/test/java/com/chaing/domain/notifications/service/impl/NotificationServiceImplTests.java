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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTests {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationStatusRepository notificationStatusRepository;
    @Mock private ObjectProvider<NotificationService> selfProvider;
    @Mock private NotificationService selfProxy;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private final Long userId = 1L;
    private final Long targetId = 100L;
    private Notification personalNotification;
    private Notification noticeNotification;

    @BeforeEach
    void setUp() {
        personalNotification = Notification.builder()
                .notificationId(1L).userId(userId).message("개인 알림").type(NotificationType.NOTICE).targetId(targetId).build();
        noticeNotification = Notification.builder()
                .notificationId(2L).userId(0L).message("전체 공지").type(NotificationType.NOTICE).targetId(targetId).build();
    }

    @Test
    @DisplayName("전체 알림 전송")
    void sendToAll() {

        // given
        NotificationEvent event = NotificationEvent.ofAll(NotificationType.NOTICE, "공지", targetId);
        given(selfProvider.getIfAvailable()).willReturn(selfProxy);

        // when
        notificationService.sendToAll(event);

        // then
        verify(notificationRepository).save(argThat(n -> n.getUserId() == 0L));
        verify(selfProxy).retryableSseSendToAll(event);
    }

    @Test
    @DisplayName("단일 유저 알림 전송")
    void sendToUser() throws Exception {

        // given
        NotificationEvent event = new NotificationEvent(userId, NotificationType.NOTICE, "비밀번호 수정", targetId, false, false);

        // when
        notificationService.sendToUser(event);

        // then
        verify(notificationRepository).save(argThat(n -> n.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("알림 상세 조회")
    void readNotification() {

        // given
        given(notificationRepository.findByIdAndUserIdOrAll(1L, userId)).willReturn(Optional.of(personalNotification));
        given(notificationStatusRepository.findByUserIdAndNotificationId(userId, 1L)).willReturn(Optional.empty());

        // when
        Notification result = notificationService.readNotification(1L, userId);

        // then
        assertThat(result).isNotNull();
        verify(notificationStatusRepository).save(argThat(NotificationStatus::isRead));
    }

    @Test
    @DisplayName("존재하지 않는 알림일 경우 예외 발생")
    void readNotification_NotFound() {

        // given
        given(notificationRepository.findByIdAndUserIdOrAll(anyLong(), anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.readNotification(999L, userId))
                .isInstanceOf(NotificationException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("읽음 상태 맵 조회")
    void getReadStatusMap() {

        // given
        List<Long> ids = List.of(1L, 2L);
        NotificationStatus status1 = NotificationStatus.builder().notificationId(1L).isRead(true).build();
        given(notificationStatusRepository.findAllByUserIdAndNotificationIdIn(userId, ids)).willReturn(List.of(status1));

        // when
        Map<Long, Boolean> result = notificationService.getReadStatusMap(userId, ids);

        // then
        assertThat(result.get(1L)).isTrue();
        assertThat(result.get(2L)).isFalse();
    }

    @Test
    @DisplayName("미읽음 알림 수 조회")
    void getUnreadCount() {

        // given
        given(notificationRepository.countUnreadByType(eq(userId), any())).willReturn(5L);
        given(notificationRepository.countTotalByType(eq(userId), any())).willReturn(10L);

        // when
        Map<String, Map<String, Long>> result = notificationService.getUnreadCount(userId);

        // then
        assertThat(result).containsKey("all");
        assertThat(result.get("all").get("unread")).isEqualTo(5L);
        assertThat(result).containsKey(NotificationType.NOTICE.name());
        verify(notificationRepository, atLeastOnce()).countUnreadByType(eq(userId), any());
    }

    @Test
    @DisplayName("알림 수정")
    void updateNotification() {

        // given
        given(notificationRepository.findByTypeAndTargetId(NotificationType.NOTICE, targetId)).willReturn(Optional.of(noticeNotification));
        given(selfProvider.getIfAvailable()).willReturn(selfProxy);

        // when
        notificationService.updateNotification(NotificationType.NOTICE, targetId, "수정 메시지");

        // then
        verify(notificationRepository).delete(noticeNotification);
        verify(notificationStatusRepository).deleteAllByNotificationId(noticeNotification.getNotificationId());
        verify(notificationRepository).save(argThat(n -> n.getMessage().equals("수정 메시지")));
        verify(selfProxy).retryableSseSendToAll(any());
    }

    @Test
    @DisplayName("알림 전체 읽음 처리")
    void markAllAsRead() {

        // given
        given(notificationRepository.findAllUnreadNotificationsList(userId)).willReturn(List.of(personalNotification));
        given(notificationStatusRepository.findByUserIdAndNotificationId(any(), any())).willReturn(Optional.empty());

        // when
        notificationService.markAllAsRead(userId);

        // then
        verify(notificationStatusRepository).save(argThat(NotificationStatus::isRead));
    }

    @Test
    @DisplayName("타겟 기반 알림 일괄 삭제")
    void deleteNotificationsByTarget() {

        // given
        List<Long> ids = List.of(10L, 11L);
        given(notificationRepository.findAllIdsByTypeAndTargetId(NotificationType.NOTICE, targetId)).willReturn(ids);

        // when
        notificationService.deleteNotificationsByTarget(NotificationType.NOTICE, targetId);

        // then
        verify(notificationStatusRepository).deleteAllByNotificationIdIn(ids);
        verify(notificationRepository).deleteAllByIdInBatch(ids);
    }

    @Test
    @DisplayName("개인 알림 삭제")
    void deleteNotification_Personal_HardDelete() {

        // given
        given(notificationRepository.findByIdAndUserIdOrAll(1L, userId)).willReturn(Optional.of(personalNotification));

        // when
        notificationService.deleteNotification(1L, userId);

        // then
        verify(notificationStatusRepository).deleteByUserIdAndNotificationId(userId, 1L);
        verify(notificationRepository).delete(personalNotification);
    }

    @Test
    @DisplayName("공지사항 삭제")
    void deleteNotification() {

        // given
        given(notificationRepository.findByIdAndUserIdOrAll(2L, userId)).willReturn(Optional.of(noticeNotification));
        given(notificationStatusRepository.findByUserIdAndNotificationId(userId, 2L)).willReturn(Optional.empty());

        // when
        notificationService.deleteNotification(2L, userId);

        // then
        verify(notificationStatusRepository).save(argThat(NotificationStatus::isDeleted));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("알림 목록 조회")
    void getNotificationList() {

        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(personalNotification));
        given(notificationRepository.findAllMyNotificationsByType(userId, null, pageable)).willReturn(page);

        // when
        Page<Notification> result = notificationService.getNotificationList(userId, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findAllMyNotificationsByType(userId, null, pageable);
    }
}