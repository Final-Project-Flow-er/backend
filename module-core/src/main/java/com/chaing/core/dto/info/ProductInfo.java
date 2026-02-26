package com.chaing.core.dto.info;

import lombok.Builder;

@Builder
public record ProductInfo(
        Long productId,

        String productCode,

        String productName
) {
}
