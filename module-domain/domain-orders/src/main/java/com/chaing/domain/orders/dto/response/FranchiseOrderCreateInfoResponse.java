package com.chaing.domain.orders.dto.response;

import lombok.Builder;

@Builder
public record FranchiseOrderCreateInfoResponse(
        String username,

        String phoneNumber,

        String address
) {
}
