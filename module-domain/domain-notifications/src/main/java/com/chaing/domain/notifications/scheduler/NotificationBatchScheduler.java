package com.chaing.domain.notifications.scheduler;

import com.chaing.domain.notifications.repository.NotificationRepository;
import com.chaing.domain.notifications.repository.NotificationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationBatchScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationStatusRepository notificationStatusRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        notificationStatusRepository.deleteOldStatuses(sixMonthsAgo);
        notificationRepository.deleteOldNotifications(sixMonthsAgo);
    }
}
