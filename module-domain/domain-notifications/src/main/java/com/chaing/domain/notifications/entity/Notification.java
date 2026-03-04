package com.chaing.domain.notifications.entity;

import com.chaing.core.entity.BaseEntity;
import com.chaing.domain.notifications.dto.command.NotificationCreateCommand;
import com.chaing.domain.notifications.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private Long targetId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    public void read() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public static Notification createNotification(NotificationCreateCommand command, Long targetId) {
        return Notification.builder()
                .userId(command.userId())
                .type(command.type())
                .message(command.message())
                .targetId(targetId)
                .isRead(false)
                .build();
    }

    public void updateContent(String message) {
        this.message = message;
    }
}
