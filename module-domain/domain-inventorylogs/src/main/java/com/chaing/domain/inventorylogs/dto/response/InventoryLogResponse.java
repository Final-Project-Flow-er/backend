package com.chaing.domain.inventorylogs.dto.response;

import com.chaing.core.enums.LogType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryLogResponse(
        // 날짜
        LocalDateTime date,

        // 반품 코드 (발주코드/반품코드/없을 수도 있음)
        String serialCode,

        // 박스 코드
        String boxCode,

        // 제품 명
        String productName,

        // 유형 (반품입고 / 반품출고 / 폐기)
        LogType logType,

        // 수량 (박스)
        Integer boxQuantity,

        // 보낸 곳
        String fromLocationCode,

        // 받는 곳
        String toLocationCode,

        // 공급가
        BigDecimal supplyPrice,

        // 변경 수량 (개)
        Integer changedQuantity
) {
}
