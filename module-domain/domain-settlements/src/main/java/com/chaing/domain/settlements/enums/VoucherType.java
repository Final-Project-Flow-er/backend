package com.chaing.domain.settlements.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VoucherType {
    SALES, // 매출
    ORDER, // 발주대금
    DELIVERY, // 배송비
    COMMISSION, // 수수료
    LOSS, // 손실
    REFUND, // 반품환급
    ADJUSTMENT // 조정
}
