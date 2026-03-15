package com.chaing.domain.sales.dto.request;

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
        BigDecimal unitPrice,

        @NotBlank
        String serialCode
) {
}
