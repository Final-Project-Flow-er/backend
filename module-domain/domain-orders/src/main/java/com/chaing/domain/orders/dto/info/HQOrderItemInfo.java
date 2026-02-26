package com.chaing.domain.orders.dto.info;

import java.math.BigDecimal;

public record HQOrderItemInfo(
        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {
}
