package com.chaing.domain.inventories.dto.response;

public record FranchiseInventoryProductResponse(

        Long productId,
        String productCode,
        String productName,
        Integer totalQuantity,
        String size,
        Integer safetyStock,
        String status

) {}