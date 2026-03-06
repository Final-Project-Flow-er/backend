package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

@Builder
public record FactoryOrderUpdateResponse(
        String orderCode,

        HQOrderStatus status
) {
}
