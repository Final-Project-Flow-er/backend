package com.chaing.domain.products.dto.request;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InventoryItemsRequest(
        Long productId,
        String serialCode,
        LocalDate productDate,
        LocalDate shippedAt,
        LocalDate receivedAt
) {
}
