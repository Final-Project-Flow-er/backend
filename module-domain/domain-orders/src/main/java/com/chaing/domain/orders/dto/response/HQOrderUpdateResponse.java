package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record HQOrderUpdateResponse(
        HQOrderCommand orderInfo,

        List<HQOrderItemCommand> items
) {
}
