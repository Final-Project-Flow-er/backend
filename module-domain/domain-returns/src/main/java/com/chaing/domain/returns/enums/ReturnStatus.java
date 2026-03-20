package com.chaing.domain.returns.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReturnStatus {

    PENDING("PENDING", "대기"),
    ACCEPTED("ACCEPTED", "접수"),
    SHIPPING_PENDING("SHIPPING_PENDING", "배송대기"),
    SHIPPING("SHIPPING", "배송중"),
    COMPLETED("COMPLETED", "배송완료"),
    INSPECTING("INSPECTING", "검수중"),
    DEDUCTION_COMPLETED("DEDUCTION_COMPLETED", "대금 차감 완료"),
    DEDUCTION_REJECTED("DEDUCTION_REJECTED", "대금 차감 거절"),
    CANCELED("CANCELED", "취소");

    private final String key;
    private final String title;
}
