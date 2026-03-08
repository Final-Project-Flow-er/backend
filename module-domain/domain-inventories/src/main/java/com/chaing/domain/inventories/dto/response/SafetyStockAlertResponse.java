package com.chaing.domain.inventories.dto.response;

import lombok.Builder;

@Builder
public record SafetyStockAlertResponse(
        String productCode,
        String productName,
        Integer currentQuantity,
        Integer safetyStock
) {
}
