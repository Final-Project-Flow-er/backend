package com.chaing.domain.inventorylogs.dto.response;

import com.chaing.core.enums.LogType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseInventoryLogResponse(
        // 날짜
        LocalDateTime date,

        // 반품 코드 (발주코드/반품코드/없을 수도 있음)
        String serialCode,

        // 박스 코드
        String boxCode,

        // 제품 명
        String productName,

        // 유형
        LogType logType,

        // 수량 (박스), 개
        Integer quantity,

        // 공급가, 판매가
        BigDecimal price,

        // 변경 수량 (개)
        Integer changedQuantity
) {
}
