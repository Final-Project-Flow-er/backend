package com.chaing.domain.orders.dto.info;

import com.chaing.domain.orders.enums.HQOrderStatus;

import java.time.LocalDateTime;

public record HQOrderInfo(
        Long orderId,

        String orderCode,

        HQOrderStatus status,

        Integer quantity,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        LocalDateTime manufacturedDate,

        String storedDate
) {
}
