package com.chaing.domain.notifications.repository;

import com.chaing.domain.notifications.entity.NotificationStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationStatusRepository extends CrudRepository<NotificationStatus, Long> {

    Optional<NotificationStatus> findByUserIdAndNotificationId(Long userId, Long notificationId);
    void deleteByUserIdAndNotificationId(Long userId, Long notificationId);
    void deleteAllByNotificationIdIn(List<Long> notificationIds);
    void deleteAllByNotificationId(Long notificationId);
    List<NotificationStatus> findAllByUserIdAndNotificationIdIn(Long userId, List<Long> notificationIds);

    @Modifying
    @Query("DELETE FROM NotificationStatus s WHERE s.notificationId IN " +
            "(SELECT n.notificationId FROM Notification n WHERE n.createdAt < :targetDate)")
    void deleteOldStatuses(@Param("targetDate") LocalDateTime targetDate);
}
