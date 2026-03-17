package com.chaing.domain.orders.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HQOrderStatus {

    PENDING("PENDING", "대기"),
    ACCEPTED("ACCEPTED", "접수"),
    CANCELED("CANCELED", "취소"),
    REJECTED("REJECTED", "반려");

    private final String key;
    private final String title;
}
