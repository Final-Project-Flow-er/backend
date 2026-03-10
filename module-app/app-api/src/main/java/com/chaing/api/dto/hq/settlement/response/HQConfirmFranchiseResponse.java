package com.chaing.api.dto.hq.settlement.response;

import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record HQConfirmFranchiseResponse(
        @Schema(description = "가맹점 ID", example = "1")
        Long franchiseId,

        @Schema(description = "가맹점명", example = "강남점")
        String franchiseName,

        @Schema(description = "최종 정산 금액", example = "4500000")
        Long finalSettlementAmount,

        @Schema(description = "정산 상태", example = "CONFIRMED")
        SettlementStatus status) {
    public static HQConfirmFranchiseResponse of(Long franchiseId, String franchiseName,
            Long finalSettlementAmount, SettlementStatus status) {
        return HQConfirmFranchiseResponse.builder()
                .franchiseId(franchiseId)
                .franchiseName(franchiseName)
                .finalSettlementAmount(finalSettlementAmount)
                .status(status)
                .build();
    }
}
