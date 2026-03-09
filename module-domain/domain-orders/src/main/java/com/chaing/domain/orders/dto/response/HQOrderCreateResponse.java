package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record HQOrderCreateResponse(
        HQOrderCommand orderInfo,

        List<HQOrderItemInfo> items
) {
}
