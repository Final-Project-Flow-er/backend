package com.chaing.api.dto.franchise.settlement.response;

import java.math.BigDecimal;

public record FranchiseOrderItemResponse(
        int rank,                      // 순위
        String productName,            // 제품명
        int totalQuantity,             // 총 수량
        BigDecimal unitPrice,          // 개당 가격
        BigDecimal totalAmount         // 총 금액

) {
}
