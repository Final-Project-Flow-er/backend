package com.chaing.domain.orders.dto.info;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record HQOrderItemInfo(
        Long productId,

        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {
}
