package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HQOrderItemCreateCommand(
        @NotNull
        String productCode,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
