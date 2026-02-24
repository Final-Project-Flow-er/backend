package com.chaing.api.dto.hq.inventories.request;

import com.chaing.core.enums.LogType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record HQInventoryBoxRequest(
        String locationType,    // 출발지 혹은 도착지
        String locationCode,    // 출발지 혹은 도착지
        String boxCode,
        LogType logType,
        BigDecimal supplyPrice, // 공급가
        LocalDateTime processedAt,    // 배송 완료 일자
        LocalDateTime receivedAt,    // 입고 완료 일자
        List<HQInventoryRequest> productList
) {
}
