package com.chaing.api.dto.franchise.settlement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record FranchiseSettlementDailySummaryRequest(
        @Schema(description = "조회 날짜", example = "2026-02-26", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date
)
{}
