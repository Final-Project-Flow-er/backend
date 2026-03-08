package com.chaing.domain.inventories.dto.request;

import java.time.LocalDate;

public record FranchiseInventoryItemsRequest(
        Long productId,
        String serialCode,
        String boxCode,
        LocalDate manufactureDate,
        LocalDate shippedAt,
        LocalDate receivedAt
) {
}
