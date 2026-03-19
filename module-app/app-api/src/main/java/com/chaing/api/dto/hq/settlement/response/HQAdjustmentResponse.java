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

        @Schema(description = "처리 방식(증가/차감)", example = "INCREASE")
        com.chaing.domain.settlements.enums.AdjustmentDirection direction,

        @Schema(description = "조정 사유", example = "본사 이벤트 지원금")
        String reason,

        @Schema(description = "정산 반영월", example = "2026-04")
        String settlementMonth,

        @Schema(description = "반품 사유", example = "PRODUCT_DEFECT")
        com.chaing.domain.returns.enums.ReturnType returnType) {
    public static HQAdjustmentResponse of(Long adjustmentId, String franchiseName, VoucherType type,
            LocalDate occurredAt, Long amount, com.chaing.domain.settlements.enums.AdjustmentDirection direction,
            String reason, String settlementMonth, com.chaing.domain.returns.enums.ReturnType returnType) {
        return HQAdjustmentResponse.builder()
                .adjustmentId(adjustmentId)
                .franchiseName(franchiseName)
                .type(type)
                .occurredAt(occurredAt)
                .amount(amount)
                .direction(direction)
                .reason(reason)
                .settlementMonth(settlementMonth)
                .returnType(returnType)
                .build();
    }
}
