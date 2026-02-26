package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import com.chaing.domain.orders.enums.HQOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record HQOrderDetailResponse(
        String orderCode,

        String franchiseCode,

        LocalDateTime requestedDate,

        String storedDate,

        HQOrderStatus status,

        String username,

        String phoneNumber,

        LocalDateTime manufacturedDate,

        String description,

        List<HQOrderItemInfo> items
) {
}
