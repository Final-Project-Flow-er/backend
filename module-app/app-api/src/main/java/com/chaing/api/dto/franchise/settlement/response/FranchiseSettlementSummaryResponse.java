package com.chaing.api.dto.franchise.settlement.response;

import java.math.BigDecimal;

public record FranchiseSettlementSummaryResponse(
                String franchiseName, // 가맹점명
                BigDecimal finalAmount, // 최종 정산 금액
                BigDecimal totalSaleAmount, // 총 매출
                BigDecimal refundAmount, // 반품 환급
                BigDecimal orderAmount, // 발주 대금
                BigDecimal deliveryFee, // 배송비
                BigDecimal lossAmount, // 손실
                BigDecimal commissionFee, // 수수료
                BigDecimal adjustmentAmount // 기타 조정
) {
}
