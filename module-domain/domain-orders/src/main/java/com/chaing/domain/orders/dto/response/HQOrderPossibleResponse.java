package com.chaing.domain.orders.dto.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record HQOrderPossibleResponse(
        // Map<returnCode, possible/impossible>
        Map<String, Boolean> isPossibleByReturnCode
) {
}
