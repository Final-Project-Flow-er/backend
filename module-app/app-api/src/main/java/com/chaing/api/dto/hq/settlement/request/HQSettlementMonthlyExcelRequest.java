package com.chaing.api.dto.hq.settlement.request;

import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record HQSettlementMonthlyExcelRequest(
        @Schema(description = "조회 월(yyyy-MM)", example = "2026-02", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
        @Schema(description = "전표 타입 필터(없으면 전체)", example = "SALES") VoucherType type) {
}
