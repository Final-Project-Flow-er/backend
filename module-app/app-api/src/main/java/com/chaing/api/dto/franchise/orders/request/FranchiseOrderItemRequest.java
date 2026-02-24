package com.chaing.api.dto.franchise.orders.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FranchiseOrderItemRequest(
        @NotNull
        String serialCode,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        BigDecimal unitPrice
) {
}
