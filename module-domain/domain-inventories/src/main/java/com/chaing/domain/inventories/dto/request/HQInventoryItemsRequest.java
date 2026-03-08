package com.chaing.domain.inventories.dto.request;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HQInventoryItemsRequest(
        Long productId,
        String serialCode,
        LocalDate manufactureDate,
        LocalDate shippedAt,
        LocalDate receivedAt
) {
}
