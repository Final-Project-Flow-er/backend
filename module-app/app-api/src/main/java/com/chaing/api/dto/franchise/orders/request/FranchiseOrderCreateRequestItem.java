package com.chaing.api.dto.franchise.orders.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FranchiseOrderCreateRequestItems(
        @NotBlank
        String productCode,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
