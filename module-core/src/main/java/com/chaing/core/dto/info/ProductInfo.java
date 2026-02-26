package com.chaing.core.dto.info;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductInfo(
        Long productId,

        String productCode,

        String productName,

        BigDecimal retailPrice,

        BigDecimal costPrice,

        BigDecimal tradePrice
) {
}
