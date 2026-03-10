package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record HQConfirmStatusCountResponse(
        @Schema(description = "정산완료 건수", example = "10") Long calculatedCount,

        @Schema(description = "확정요청 건수", example = "5") Long requestedCount,

        @Schema(description = "최종확정 건수", example = "45") Long finalizedCount) {
    public static HQConfirmStatusCountResponse of(
            Long calculatedCount,
            Long requestedCount,
            Long finalizedCount) {
        return HQConfirmStatusCountResponse.builder()
                .calculatedCount(calculatedCount)
                .requestedCount(requestedCount)
                .finalizedCount(finalizedCount)
                .build();
    }
}
