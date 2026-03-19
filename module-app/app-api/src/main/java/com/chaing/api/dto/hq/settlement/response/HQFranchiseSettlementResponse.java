package com.chaing.api.dto.hq.settlement.response;

import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record HQFranchiseSettlementResponse(
        @Schema(description = "가맹점 ID", example = "1") Long franchiseId,

        @Schema(description = "가맹점명", example = "강남점") String franchiseName,

        @Schema(description = "총 매출액", example = "5000000") BigDecimal totalSaleAmount,

        @Schema(description = "발주 대금", example = "2000000") BigDecimal orderAmount,

        @Schema(description = "배달비", example = "500000") BigDecimal deliveryFee,

        @Schema(description = "수수료", example = "150000") BigDecimal commissionFee,

        @Schema(description = "반품 환급", example = "100000") BigDecimal refundAmount,

        @Schema(description = "손실액", example = "50000") BigDecimal lossAmount,
        @Schema(description = "조정액", example = "10000") BigDecimal adjustmentAmount,
        @Schema(description = "최종 정산 금액", example = "4500000") BigDecimal finalAmount,

        @Schema(description = "정산 상태", example = "CONFIRMED") SettlementStatus status,

        @Schema(description = "정산 기준일", example = "2026-02-15") LocalDate settlementDate) {

    public static HQFranchiseSettlementResponse of(Long franchiseId, String franchiseName,
            BigDecimal totalSaleAmount, BigDecimal orderAmount, BigDecimal deliveryFee, BigDecimal commissionFee,
            BigDecimal refundAmount, BigDecimal lossAmount, BigDecimal adjustmentAmount, BigDecimal finalAmount, SettlementStatus status,
            LocalDate settlementDate) {
        return HQFranchiseSettlementResponse.builder()
                .franchiseId(franchiseId)
                .franchiseName(franchiseName)
                .totalSaleAmount(totalSaleAmount)
                .orderAmount(orderAmount)
                .deliveryFee(deliveryFee)
                .commissionFee(commissionFee)
                .refundAmount(refundAmount)
                .lossAmount(lossAmount)
                .adjustmentAmount(adjustmentAmount)
                .finalAmount(finalAmount)
                .status(status)
                .settlementDate(settlementDate)
                .build();
    }
}
