package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HQDailyGraphResponse(
        @Schema(description = "일자", example = "2026-02-15") LocalDate date,

        @Schema(description = "합산 매출액", example = "15000000") Long totalSaleAmount) {
    public static HQDailyGraphResponse of(LocalDate date, Long totalSaleAmount) {
        return HQDailyGraphResponse.builder()
                .date(date)
                .totalSaleAmount(totalSaleAmount)
                .build();
    }
}
