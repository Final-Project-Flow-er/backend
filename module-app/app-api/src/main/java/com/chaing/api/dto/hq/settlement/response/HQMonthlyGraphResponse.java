package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.YearMonth;

@Builder
public record HQMonthlyGraphResponse(
        @Schema(description = "월(yyyy-MM)", example = "2026-02") 
        YearMonth month,
        
        @Schema(description = "합산 매출액", example = "45000000") 
        Long totalSaleAmount
) {
    public static HQMonthlyGraphResponse of(YearMonth month, Long totalSaleAmount) {
        return HQMonthlyGraphResponse.builder()
                .month(month)
                .totalSaleAmount(totalSaleAmount)
                .build();
    }
}
