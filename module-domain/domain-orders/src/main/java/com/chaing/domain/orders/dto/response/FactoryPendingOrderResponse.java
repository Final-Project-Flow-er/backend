package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;

import java.time.LocalDateTime;

public record FactoryPendingOrderResponse(
        String orderCode,

        HQOrderStatus status,

        Boolean isRegular,

        String productCode,

        String productName,

        Integer quantity,

        String username,

        String phoneNumber,

        String employeeNumber,

        LocalDateTime requestedDate,

        LocalDateTime storedDate
) {
}
