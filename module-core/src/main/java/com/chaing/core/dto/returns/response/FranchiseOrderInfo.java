package com.chaing.core.dto.returns.response;

import lombok.Builder;

@Builder
public record FranchiseOrderInfo(
        String orderCode,

        String username,

        String phoneNumber,

        String franchiseCode
) {
}
