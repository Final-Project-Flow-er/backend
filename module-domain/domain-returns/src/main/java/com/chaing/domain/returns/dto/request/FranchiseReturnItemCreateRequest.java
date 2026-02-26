package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FranchiseReturnItemCreateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        BigDecimal unitPrice
) {
}
