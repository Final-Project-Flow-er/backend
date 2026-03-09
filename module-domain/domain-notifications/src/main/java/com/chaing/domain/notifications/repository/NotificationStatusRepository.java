package com.chaing.domain.notifications.repository;

import com.chaing.domain.notifications.entity.NotificationStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationStatusRepository extends CrudRepository<NotificationStatus, Long> {

    Optional<NotificationStatus> findByUserIdAndNotificationId(Long userId, Long notificationId);
    void deleteByUserIdAndNotificationId(Long userId, Long notificationId);
    void deleteAllByNotificationId(Long notificationId);
}
