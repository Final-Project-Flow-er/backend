package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;

import java.util.List;

public record HQOrderUpdateResponse(
        HQOrderInfo orderInfo,

        List<HQOrderItemInfo> items
) {
}
