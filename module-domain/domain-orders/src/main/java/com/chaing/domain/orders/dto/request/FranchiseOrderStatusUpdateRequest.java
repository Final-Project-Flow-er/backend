package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FranchiseOrderStatusUpdateRequest(
        @NotBlank
        String orderCode
) {
}
