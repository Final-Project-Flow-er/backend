package com.chaing.domain.inventories.dto.response;

public record SafetyStockResponse(
        Long productId,
        Integer currentQuantity,
        Integer safetyStock
) {
}
