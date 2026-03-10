package com.chaing.api.dto.hq.settlement.response;

import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HQFranchiseSettlementResponse(
        @Schema(description = "가맹점 ID", example = "1") Long franchiseId,

        @Schema(description = "가맹점명", example = "강남점") String franchiseName,

        @Schema(description = "총 매출액", example = "5000000") Long totalSaleAmount,

        @Schema(description = "최종 정산 금액", example = "4500000") Long finalAmount,

        @Schema(description = "정산 상태", example = "CONFIRMED") SettlementStatus status,

        @Schema(description = "정산 기준일", example = "2026-02-15") LocalDate settlementDate) {

    public static HQFranchiseSettlementResponse of(Long franchiseId, String franchiseName, Long totalSaleAmount,
            Long finalAmount, SettlementStatus status, LocalDate settlementDate) {
        return HQFranchiseSettlementResponse.builder()
                .franchiseId(franchiseId)
                .franchiseName(franchiseName)
                .totalSaleAmount(totalSaleAmount)
                .finalAmount(finalAmount)
                .status(status)
                .settlementDate(settlementDate)
                .build();
    }
}
