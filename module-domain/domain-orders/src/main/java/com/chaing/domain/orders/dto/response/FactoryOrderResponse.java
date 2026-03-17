package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FactoryOrderResponse(
        String orderCode,

        HQOrderStatus status,

        Boolean isRegular,

        String productCode,

        String productName,

        Integer quantity,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        String storedDate
) {
}
