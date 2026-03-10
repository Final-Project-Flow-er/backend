package com.chaing.domain.inventories.dto.info;

public record OutboundGetBoxInfo(
        String boxCode,
        Long productId,
        Long countItem,
        Long orderId
) {
}
