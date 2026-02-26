package com.chaing.core.dto.returns.request;

import lombok.Builder;

@Builder
public record OrderItemIdAndSerialCode(
        Long orderItemId,

        String serialCode
) {
}
