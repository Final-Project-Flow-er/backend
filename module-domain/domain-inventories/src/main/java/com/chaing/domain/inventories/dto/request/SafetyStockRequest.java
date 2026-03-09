package com.chaing.domain.inventories.dto.request;

public record SafetyStockRequest(
        String locationType,
        Long locationId,
        Long productId,
        Integer safetyStock
) {
}
