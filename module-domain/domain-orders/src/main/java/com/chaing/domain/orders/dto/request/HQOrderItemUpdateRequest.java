package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HQOrderItemUpdateRequest(
        @NotBlank
        String productCode,

        @NotNull
        Integer quantity
) {
}
