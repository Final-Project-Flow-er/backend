package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SettlementStatus {
    DRAFT("정산 생성"),
    CALCULATED("정산 완료"),
    CONFIRM_REQUESTED("확정 요청"),
    CONFIRMED("본사 확정 완료"),
    CANCELED("취소");

    private String description;
}
