package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.transports.dto.OrderInfo;

import java.time.LocalDateTime;

public record UnassignedOrderResponse(
        Long orderId,
        String orderCode,
        String franchiseName,
        String representativeName,
        String address,
        Long weight,
        LocalDateTime orderCreatedAt,
        LocalDateTime deliveryDate
) {
    public static UnassignedOrderResponse from (OrderInfo orderInfo, BusinessUnitInternal franchiseInfo) {
        return new UnassignedOrderResponse(orderInfo.orderId(), orderInfo.orderCode(), franchiseInfo.name()
                , franchiseInfo.name(), franchiseInfo.address(), orderInfo.weight()
                , orderInfo.orderCreatedAt(), orderInfo.deliveryDate());
    }
}
