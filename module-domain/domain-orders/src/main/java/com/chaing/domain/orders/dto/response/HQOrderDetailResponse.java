package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record HQOrderDetailResponse(
        HQOrderInfo orderInfo,

        List<HQOrderItemInfo> items
) {
}
