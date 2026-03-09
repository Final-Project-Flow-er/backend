package com.chaing.domain.notifications.entity;

import com.chaing.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "notification_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_status_user_notification",
                        columnNames = {"userId", "notificationId"}
                )
        }
)
public class NotificationStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long notificationId;

    @Column(nullable = false)
    private boolean isRead;

    public void read() {
        this.isRead = true;
    }
}
