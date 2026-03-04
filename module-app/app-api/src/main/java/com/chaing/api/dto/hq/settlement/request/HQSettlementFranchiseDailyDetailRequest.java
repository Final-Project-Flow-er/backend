package com.chaing.api.dto.hq.settlement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record HQSettlementFranchiseDailyDetailRequest(
        @Schema(description = "가맹점 ID (PathVariable)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long franchiseId,

        @Schema(description = "조회 월(yyyy-MM)", example = "2026-02", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month
) {
}
