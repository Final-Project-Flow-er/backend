package com.chaing.domain.inventories.dto.response;

import java.time.LocalDateTime;

public record FranchiseInventoryItemResponse(
                Long inventoryId,
                String serialCode,
                String boxCode,
                String status,
                LocalDateTime shippedAt,
                LocalDateTime receivedAt) {
}