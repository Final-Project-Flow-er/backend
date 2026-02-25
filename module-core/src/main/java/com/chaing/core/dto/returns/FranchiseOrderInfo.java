package com.chaing.core.dto.returns;

import lombok.Builder;

@Builder
public record FranchiseOrderInfo(
        Long orderId,

        String username,

        String phoneNumber,

        String franchiseCode
) {
}
