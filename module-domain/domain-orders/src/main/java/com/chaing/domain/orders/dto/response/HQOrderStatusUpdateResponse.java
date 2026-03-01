package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import jakarta.validation.constraints.NotBlank;

public record HQOrderStatusUpdateResponse(
        @NotBlank
        String orderCode,

        @NotBlank
        FranchiseOrderStatus status
) {
}
