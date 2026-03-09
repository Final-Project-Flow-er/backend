package com.chaing.core.dto.command;

import lombok.Builder;

@Builder
public record FranchiseInventoryCommand(
        Long inventoryId,

        Long orderId,

        Long orderItemId,

        String serialCode,

        Long productId,

        String boxCode
) {
}
