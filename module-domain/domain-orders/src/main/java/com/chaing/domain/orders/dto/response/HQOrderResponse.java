package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HQOrderResponse(
        String orderCode,

        HQOrderStatus status,

        Integer quantity,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        LocalDateTime manufacturedDate,

        String storedDate,

        String productCode
) {

}
