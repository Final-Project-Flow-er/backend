package com.chaing.api.dto.hq.settlement.response;

import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HQAdjustmentResponse(
        @Schema(description = "조정 전표 ID", example = "100")
        Long adjustmentId,

        @Schema(description = "가맹점명", example = "강남점")
        String franchiseName,

        @Schema(description = "조정 유형", example = "LOSS_COMPENSATION")
        VoucherType type,

        @Schema(description = "발생일자", example = "2026-02-20")
        LocalDate occurredAt,

        @Schema(description = "조정 금액", example = "50000")
        Long amount,

        @Schema(description = "차감 여부(true면 마이너스 처리)", example = "false")
        Boolean isMinus,

        @Schema(description = "조정 사유", example = "본사 이벤트 지원금")
        String reason) {
    public static HQAdjustmentResponse of(Long adjustmentId, String franchiseName, VoucherType type,
            LocalDate occurredAt, Long amount, Boolean isMinus, String reason) {
        return HQAdjustmentResponse.builder()
                .adjustmentId(adjustmentId)
                .franchiseName(franchiseName)
                .type(type)
                .occurredAt(occurredAt)
                .amount(amount)
                .isMinus(isMinus)
                .reason(reason)
                .build();
    }
}
