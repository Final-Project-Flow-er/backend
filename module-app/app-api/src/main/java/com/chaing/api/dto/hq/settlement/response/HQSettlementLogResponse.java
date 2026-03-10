package com.chaing.api.dto.hq.settlement.response;

import com.chaing.domain.settlements.enums.SettlementLogType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HQSettlementLogResponse(
        @Schema(description = "로그 ID(번호)", example = "1")
        Long id,

        @Schema(description = "로그 유형", example = "CONFIRM")
        SettlementLogType type,

        @Schema(description = "가맹점명", example = "강남점")
        String franchiseName,

        @Schema(description = "처리 내역", example = "본사 강남점 2026-02월 정산 최종 확정 처리")
        String content,

        @Schema(description = "처리자 이름", example = "김본사")
        String actorName,

        @Schema(description = "작업 일시", example = "2026-03-10T15:30:00")
        LocalDateTime createdAt) {
    public static HQSettlementLogResponse of(Long id, SettlementLogType type, String franchiseName,
            String content, String actorName, LocalDateTime createdAt) {
        return HQSettlementLogResponse.builder()
                .id(id)
                .type(type)
                .franchiseName(franchiseName)
                .content(content)
                .actorName(actorName)
                .createdAt(createdAt)
                .build();
    }
}
