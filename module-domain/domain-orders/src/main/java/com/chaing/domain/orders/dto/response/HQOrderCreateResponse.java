package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import com.chaing.domain.orders.enums.HQOrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record HQOrderCreateResponse(
        String orderCode,

        HQOrderStatus status,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        LocalDateTime manufacturedDate,

        String storedDate,

        String description,

        Boolean isRegular,

        List<HQOrderItemCommand> items
) {
}
