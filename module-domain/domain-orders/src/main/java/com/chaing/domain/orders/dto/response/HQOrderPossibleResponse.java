package com.chaing.domain.orders.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record HQOrderPossibleResponse(
        String orderCode,

        Boolean isPossible
) {
}
