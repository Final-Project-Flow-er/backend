package com.chaing.domain.orders.dto.info;

import lombok.Builder;

@Builder
public record ProductInfo(
        Long productId,

        String productCode,

        String productName
) {
}
