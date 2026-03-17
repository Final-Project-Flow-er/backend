package com.chaing.core.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FranchiseOrderCreateRequestItem(
        @NotBlank
        String productCode,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
