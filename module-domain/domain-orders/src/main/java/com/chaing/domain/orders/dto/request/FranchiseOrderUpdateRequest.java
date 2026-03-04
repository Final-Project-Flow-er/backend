package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FranchiseOrderUpdateRequest(
        @NotBlank
        String productCode,

        @NotNull
        Integer quantity
) {

}
