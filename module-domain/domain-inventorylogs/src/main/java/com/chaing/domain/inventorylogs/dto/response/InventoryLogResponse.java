package com.chaing.domain.inventorylogs.dto.response;

import com.chaing.core.enums.LogType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryLogResponse(
        // 날짜
        LocalDateTime date,

        // 반품 코드 (발주코드/반품코드/없을 수도 있음)
        String transactionCode,

        // 박스 코드
        String boxCode,

        // 제품 명
        String productName,

        // 유형 (반품입고 / 반품출고 / 폐기)
        LogType logType,

        // 보낸 곳
        Long fromLocationId,

        // 받는 곳
        Long toLocationId,

        // 변경 수량 (개)
        Integer changedQuantity
) {
}
