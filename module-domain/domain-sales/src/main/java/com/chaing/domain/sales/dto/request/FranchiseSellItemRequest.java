package com.chaing.domain.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseSellItemRequest(
        @NotNull
        Long productId,

        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        BigDecimal unitPrice,

        @NotBlank
        String serialCode
) {
}
