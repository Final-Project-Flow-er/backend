package com.chaing.domain.inventories.dto.request;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InventoryBatchRequest(
        String transactionCode,   // 참조 번호 (발주 번호: HEAD2026..., 반품 번호 등)
        LocalDateTime recordTime,
        Long fromLocationId,    // 출발지 ID
        String fromLocationType,    // 출발지
        Long toLocationId,    // 도착지 ID
        String toLocationType,    // 도착지
        Integer totalQuantity,  // 총 수량
        BigDecimal supplyPrice, // 공급가
        LocalDateTime shippedAt, // 배송 완료 시간
        List<InventoryBoxRequest> boxes
) {
}
