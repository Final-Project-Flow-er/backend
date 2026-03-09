package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;

import java.time.LocalDateTime;

public record HQRequestedOrderResponse(
        String orderCode,

        String franchiseCode,

        String receiver,

        String productCode,

        FranchiseOrderStatus status,

        Integer quantity,

        LocalDateTime deliveryDate
) {
}
