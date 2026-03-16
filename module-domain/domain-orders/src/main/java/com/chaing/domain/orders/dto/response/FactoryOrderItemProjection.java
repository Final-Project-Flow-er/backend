package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;

import java.time.LocalDateTime;

public record FactoryOrderItemProjection(
        String orderCode,
        HQOrderStatus status,
        Boolean isRegular,
        Long userId,
        Long productId,
        Integer quantity,
        LocalDateTime requestedDate,
        String storedDate
) {
}
