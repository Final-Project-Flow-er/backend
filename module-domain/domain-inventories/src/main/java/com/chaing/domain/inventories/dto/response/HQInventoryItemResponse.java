package com.chaing.domain.inventories.dto.response;

import java.time.LocalDateTime;

public record HQInventoryItemResponse(
                Long inventoryId,
                String serialCode,
                String boxCode,
                String status,
                LocalDateTime shippedAt, // 배송 완료 일자
                LocalDateTime receivedAt // 입고 완료 일자
) {
}
