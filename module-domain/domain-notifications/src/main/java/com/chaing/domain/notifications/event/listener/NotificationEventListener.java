package com.chaing.domain.notifications.event.listener;

import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {

        try {
            if (event.isUpdate()) {
                notificationService.updateNotification(event.targetId(), event.message());
            } else if (event.isAll()) {
                notificationService.sendToAll(event);
            } else {
                notificationService.sendToUser(event);
            }
        } catch (Exception e) {
            log.error("알림 발송 실패 - 대상 ID: {}, 에러 메시지: {}",
                    event.targetId(), e.getMessage(), e);
        }
    }
}
