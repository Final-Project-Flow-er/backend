package com.chaing.api.dto.factory.response;

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

        String employeeNumber,

        LocalDateTime requestedDate,

        String storedDate
) {
}
