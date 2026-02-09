package com.chaing.domain.orders.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FranchiseOrderStatus {

    PENDING("PENDING", "대기"),
    ACCEPTED("ACCEPTED", "접수"),
    PARTIAL("PARTIAL", "부분 접수"),
    SHIPPING("SHIPPING", "배송중"),
    COMPLETED("COMPLETED", "배송완료"),
    CANCELED("CANCELED", "취소"),
    REJECTED("REJECTED", "반려");

    private final String key;
    private final String title;
}
