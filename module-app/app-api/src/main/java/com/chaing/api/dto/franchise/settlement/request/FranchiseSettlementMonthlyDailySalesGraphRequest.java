package com.chaing.api.dto.franchise.settlement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;

public record FranchiseSettlementMonthlyDailySalesGraphRequest(
        @Schema(description = "조회 월(yyyy-MM)", example = "2026-02", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month,

        @Schema(description = "시작일", example = "2026-02-03", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate start,

        @Schema(description = "종료일", example = "2026-02-15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate end
) {}
