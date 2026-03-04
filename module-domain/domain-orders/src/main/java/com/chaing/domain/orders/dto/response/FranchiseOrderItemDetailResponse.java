package com.chaing.domain.orders.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseOrderItemDetailResponse(
        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {

}
