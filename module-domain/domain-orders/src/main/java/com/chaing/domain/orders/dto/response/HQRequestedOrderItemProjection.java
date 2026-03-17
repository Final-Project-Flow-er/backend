package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;

import java.time.LocalDateTime;

public record HQRequestedOrderItemProjection(
        String orderCode,
        FranchiseOrderStatus orderStatus,
        Long userId,
        Long productId,
        Integer quantity,
        LocalDateTime deliveryDate
) {
}
