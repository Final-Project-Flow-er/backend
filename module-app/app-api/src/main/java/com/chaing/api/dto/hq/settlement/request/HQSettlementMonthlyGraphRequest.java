package com.chaing.api.dto.hq.settlement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HQSettlementMonthlyGraphRequest(
        @Schema(description = "시작일", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate start,

        @Schema(description = "종료일", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate end
) {
}
