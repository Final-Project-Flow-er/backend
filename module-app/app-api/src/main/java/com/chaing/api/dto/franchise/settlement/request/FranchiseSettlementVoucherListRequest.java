package com.chaing.api.dto.franchise.settlement.request;

import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;

public record FranchiseSettlementVoucherListRequest(
        @Schema(description = "기간타입", example = "DAILY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PeriodType period,

        @Schema(description = "일별 조회 날짜 (period=DAILY일때)", example = "2026-02-26")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @Schema(description = "월별 조회 월 (period=MONTHLY일때)", example = "2026-02")
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month,

        @Schema(description = "전표 타입(없으면 전체)", example = "SALES")
        VoucherType type,

        @Schema(description = "페이지(0부터)", example = "0", defaultValue = "0")
        @Min(0)
        Integer page,

        @Schema(description = "사이즈", example = "20", defaultValue = "20")
        @Min(1)
        Integer size

) {}
