package com.chaing.core.dto.returns.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReturnToProductRequest(
        @NotNull
        Long productId,

        @NotBlank
        String productName,

        @NotBlank
        String productCode,

        @NotNull
        BigDecimal unitPrice
) {
}
