package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotNull;

public record HQOrderItemUpdateRequest(
        @NotNull
        String productCode,

        @NotNull
        Integer quantity
) {
}
