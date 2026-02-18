package com.chaing.domain.orders.dto.info;

import java.math.BigDecimal;

public record FranchiseOrderItemInfo(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
) {

}
