package com.chaing.domain.notifications.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    NOTICE("공지사항"),
    STOCK("재고"),
    SYSTEM("시스템");

    private final String description;
}
