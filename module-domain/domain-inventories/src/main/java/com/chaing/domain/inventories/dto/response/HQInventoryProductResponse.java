package com.chaing.domain.inventories.dto.response;

public record HQInventoryProductResponse(
        Long productId,
        String productCode,
        String productName,
        Integer totalQuantity,
        String sizeCode,
        Integer safetyStock,
        String status
) {
}
