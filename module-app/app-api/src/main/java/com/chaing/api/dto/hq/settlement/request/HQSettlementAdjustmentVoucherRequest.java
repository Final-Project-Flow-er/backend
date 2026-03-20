package com.chaing.api.dto.hq.settlement.request;

import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HQSettlementAdjustmentVoucherRequest(
        @Schema(description = "대상 가맹점 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(1)
        Long franchiseId,

        @Schema(description = "전표 유형", example = "ADJUSTMENT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        VoucherType type,

        @Schema(description = "발생일", example = "2026-02-26", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate occurredAt,

        @Schema(description = "금액(원)", example = "50000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Min(0)
        Long amount,

        @Schema(description = "증가/차감 여부", example = "INCREASE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        com.chaing.domain.settlements.enums.AdjustmentDirection direction,

        @Schema(description = "내역(조정 사유)", example = "본사 프로모션 보전금", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String reason,

        @Schema(description = "정산 반영월", example = "2026-04", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM")
        java.time.YearMonth settlementMonth,

        @Schema(description = "반품 사유 (전표 유형이 RETURN일 때 필수)", example = "PRODUCT_DEFECT")
        com.chaing.domain.returns.enums.ReturnType returnType
) {
}
