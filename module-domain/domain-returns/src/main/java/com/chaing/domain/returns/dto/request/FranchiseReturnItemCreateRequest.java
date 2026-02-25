package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FranchiseReturnItemCreateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
