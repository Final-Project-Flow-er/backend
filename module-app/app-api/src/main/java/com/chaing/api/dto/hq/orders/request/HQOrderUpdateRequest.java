package com.chaing.api.dto.hq.orders.request;

import jakarta.validation.constraints.NotNull;

public record HQOrderUpdateRequest(
        @NotNull
        String productCode,

        @NotNull
        Integer quantity
) {
}
