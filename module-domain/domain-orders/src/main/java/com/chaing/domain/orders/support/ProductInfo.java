package com.chaing.domain.orders.support;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductInfo(
    String productCode,

    Long productId,

    BigDecimal unitPrice
) {
}
