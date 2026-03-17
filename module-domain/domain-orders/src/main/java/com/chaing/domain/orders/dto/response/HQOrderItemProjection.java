package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;

import java.time.LocalDateTime;

public record HQOrderItemProjection(
        String orderCode,
        HQOrderStatus status,
        Long userId,
        Long productId,
        Integer quantity,
        LocalDateTime requestedDate,
        LocalDateTime manufacturedDate,
        String storedDate
) {
}
