package com.chaing.api.dto.hq.settlement.request;

import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HQSettlementDailyFranchisesRequest(
        @Schema(description = "조회 날짜", example = "2026-02-26", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Schema(description = "가맹점명 검색", example = "강남점")
        String keyword,

        @Schema(description = "상태 필터", example = "DRAFT")
        SettlementStatus status,

        @Schema(description = "페이지(0부터)", example = "0", defaultValue = "0")
        @Min(0)
        Integer page,

        @Schema(description = "사이즈", example = "20", defaultValue = "20")
        @Min(1)
        Integer size

) {
}
