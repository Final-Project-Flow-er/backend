package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record HQAdjustmentFranchiseResponse(
        @Schema(description = "가맹점 ID", example = "1")
        Long franchiseId,

        @Schema(description = "가맹점명", example = "강남점")
        String franchiseName) {
    public static HQAdjustmentFranchiseResponse of(Long franchiseId, String franchiseName) {
        return HQAdjustmentFranchiseResponse.builder()
                .franchiseId(franchiseId)
                .franchiseName(franchiseName)
                .build();
    }
}
