package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FranchiseOrderItemProjection(
        String orderCode,
        FranchiseOrderStatus orderStatus,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        LocalDateTime requestedDate,
        LocalDateTime deliveryDate
) {
}
