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
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN NotificationStatus s ON n.notificationId = s.notificationId AND s.userId = :userId " +
            "WHERE (n.userId = :userId OR n.userId = 0) " +
            "AND (s.deletedAt IS NULL) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findAllMyNotifications(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN NotificationStatus s ON n.notificationId = s.notificationId AND s.userId = :userId " +
            "WHERE (n.userId = :userId OR n.userId = 0) " +
            "AND (s.isRead IS NULL OR s.isRead = false)")
    List<Notification> findAllUnreadNotificationsList(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.notificationId = :notificationId " +
            "AND (n.userId = :userId OR n.userId = 0)")
    Optional<Notification> findByIdAndUserIdOrAll(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :targetDate")
    void deleteOldNotifications(@Param("targetDate") LocalDateTime targetDate);

    void deleteAllByTypeAndTargetId(NotificationType type, Long targetId);

    Optional<Notification> findByTypeAndTargetId(NotificationType type, Long targetId);
}
