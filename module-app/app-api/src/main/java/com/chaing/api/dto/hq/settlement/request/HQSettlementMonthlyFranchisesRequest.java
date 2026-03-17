package com.chaing.api.dto.hq.settlement.request;

import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record HQSettlementMonthlyFranchisesRequest(
        @Schema(description = "조회 월(yyyy-MM)", example = "2026-02", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,

        @Schema(description = "가맹점명 검색", example = "강남점") String keyword,
        @Schema(description = "상태 필터", example = "CONFIRMED") SettlementStatus status,

        @Schema(description = "페이지(0부터)", example = "0", defaultValue = "0") @Min(0) Integer page,

        @Schema(description = "사이즈", example = "20", defaultValue = "20") @Min(1) Integer size) {
}
