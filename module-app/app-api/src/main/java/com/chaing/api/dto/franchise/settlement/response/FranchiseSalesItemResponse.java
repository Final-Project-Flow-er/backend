package com.chaing.api.dto.franchise.settlement.response;

import java.math.BigDecimal;

public record FranchiseSalesItemResponse(
        int rank,                      // 순위
        String productName,            // 상품명
        int totalQuantity,             // 총 판매수량
        BigDecimal unitPrice,          // 단가
        BigDecimal totalSales          // 총 매출

) {
}
