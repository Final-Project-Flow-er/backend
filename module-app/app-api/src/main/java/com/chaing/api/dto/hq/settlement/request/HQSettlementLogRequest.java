package com.chaing.api.dto.hq.settlement.request;

import com.chaing.domain.settlements.enums.SettlementLogType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record HQSettlementLogRequest(
        @Schema(description = "탭 필터", example = "ALL", defaultValue = "ALL")
        SettlementLogType type,

        @Schema(description = "가맹점 필터", example = "1")
        Long franchiseId,

        @Schema(description = "검색어", example = "강남점")
        String keyword,

        @Schema(description = "시작 일시", example = "2026-02-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @Schema(description = "종료 일시", example = "2026-02-28T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to,

        @Schema(description = "페이지(0부터)", example = "0", defaultValue = "0")
        @Min(0)
        Integer page,

        @Schema(description = "사이즈", example = "20", defaultValue = "20")
        @Min(1)
        Integer size,

        @Schema(description = "정렬", example = "createdAt,desc", defaultValue = "createdAt,desc")
        String sort
) {
}
