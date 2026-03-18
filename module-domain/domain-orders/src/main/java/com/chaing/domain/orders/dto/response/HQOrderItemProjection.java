package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HQOrderItemProjection(
        String orderCode,
        HQOrderStatus status,
        Long userId,
        Integer totalQuantity,
        BigDecimal totalAmount,
        LocalDateTime requestedDate
) {
}
