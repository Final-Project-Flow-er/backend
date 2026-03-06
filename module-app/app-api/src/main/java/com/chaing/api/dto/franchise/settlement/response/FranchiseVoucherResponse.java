package com.chaing.api.dto.franchise.settlement.response;

import com.chaing.domain.settlements.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FranchiseVoucherResponse(
        String referenceCode,          // 전표번호
        VoucherType type,              // 유형 (판매/발주/배송/수수료/반품/손실)
        String description,            // 상품/내역
        Integer quantity,              // 수량
        BigDecimal amount,             // 금액
        LocalDateTime occurredAt       // 발생일시
) {
}
