package com.chaing.domain.notifications.repository;

import com.chaing.domain.notifications.entity.Notification;
import com.chaing.domain.notifications.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    List<Notification> findAllByUserIdAndIsReadFalse(Long userId);
    List<Notification> findAllByTypeAndTargetId(NotificationType type, Long targetId);
    Optional<Notification> findByNotificationIdAndUserId(Long notificationId, Long userId);
    void deleteAllByTypeAndTargetId(NotificationType type, Long targetId);
}
