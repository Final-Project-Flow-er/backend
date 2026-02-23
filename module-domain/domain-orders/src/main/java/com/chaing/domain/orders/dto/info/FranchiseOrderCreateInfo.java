package com.chaing.domain.orders.dto.info;

public record FranchiseOrderCreateInfo(
        String productCode,
        Integer quantity
) {
}
