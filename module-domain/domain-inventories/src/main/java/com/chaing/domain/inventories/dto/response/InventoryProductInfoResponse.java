package com.chaing.domain.inventories.dto.response;

public record InventoryProductInfoResponse(
        Long productId,
        Integer totalQuantity,
        Integer safetyStock,
        String status
) {
}
