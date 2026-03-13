package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HQFranchiseOrderCancelRequest(
        @NotBlank
        String orderCode,

        @NotBlank
        String canceledReason
) {
}
