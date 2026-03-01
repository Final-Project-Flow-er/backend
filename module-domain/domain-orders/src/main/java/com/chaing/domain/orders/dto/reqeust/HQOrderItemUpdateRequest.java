package com.chaing.domain.orders.dto.reqeust;

import jakarta.validation.constraints.NotNull;

public record HQOrderItemUpdateRequest(
        @NotNull
        Long productId,

        @NotNull
        Integer quantity
) {
}
