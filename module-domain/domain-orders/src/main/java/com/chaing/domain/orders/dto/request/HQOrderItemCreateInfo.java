package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HQOrderItemCreateInfo(
        @NotNull
        Long productId,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
