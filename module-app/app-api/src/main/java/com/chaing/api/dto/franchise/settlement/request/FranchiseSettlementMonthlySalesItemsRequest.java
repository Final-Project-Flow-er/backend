package com.chaing.api.dto.franchise.settlement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record FranchiseSettlementMonthlySalesItemsRequest(
        @Schema(description = "조회 월(yyyy-MM)", example = "2026-02", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month,

        @Schema(description = "상위 N개 제한(없으면 전체)", example = "5")
        @Min(1)
        Integer limit
) {
}
