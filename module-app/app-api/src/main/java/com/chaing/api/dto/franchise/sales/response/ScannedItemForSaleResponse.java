package com.chaing.api.dto.franchise.sales.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ScannedItemForSaleResponse(
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
    public static ScannedItemForSaleResponse create(
            Long productId, String productCode, String productName, BigDecimal unitPrice, String serialCode
    ){
        return new ScannedItemForSaleResponse(productId, productCode, productName, unitPrice, serialCode);
    }
}
