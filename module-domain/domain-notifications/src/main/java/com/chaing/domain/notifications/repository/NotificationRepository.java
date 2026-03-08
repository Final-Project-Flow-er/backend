package com.chaing.domain.notifications.repository;

import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserIdInOrderByUpdatedAtDesc(Collection<Long> userIds, Pageable pageable);
    List<Notification> findAllByUserIdInAndIsReadFalse(Collection<Long> userIds);
    void deleteAllByTypeAndTargetId(NotificationType type, Long targetId);

    @Query("SELECT n FROM Notification n WHERE n.notificationId = :notificationId AND (n.userId = :userId OR n.userId = 0)")
    Optional<Notification> findByNotificationIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :targetDate")
    void deleteOldNotifications(@Param("targetDate") LocalDateTime targetDate);
}
