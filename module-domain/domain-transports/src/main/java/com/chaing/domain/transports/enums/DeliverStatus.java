package com.chaing.domain.orders.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliverStatus {

    PENDING("배송 대기"),
    IN_TRANSIT("배송 중"),
    DELIVERED("배송 완료");

    private final String description;
}
