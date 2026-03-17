package com.chaing.domain.orders.dto.info;

import java.math.BigDecimal;

public record FranchiseOrderItemInfo(
        String serialCode,
        Integer quantity,
        BigDecimal unitPrice
) {

}
