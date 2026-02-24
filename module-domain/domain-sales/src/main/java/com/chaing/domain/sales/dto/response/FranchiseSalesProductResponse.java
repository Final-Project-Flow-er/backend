package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.SalesItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseSalesProductResponse(
        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        @Min(1)
        BigDecimal unitPrice,

        @NotNull
        @Min(1)
        BigDecimal totalPrice,

        @NotNull
        String lot
) {
    public static FranchiseSalesProductResponse from(SalesItem salesItem) {
        return FranchiseSalesProductResponse.builder()
                .productCode(salesItem.getProductCode())
                .productName(salesItem.getProductName())
                .quantity(salesItem.getQuantity())
                .unitPrice(salesItem.getUnitPrice())
                .totalPrice(salesItem.getUnitPrice().multiply(BigDecimal.valueOf(salesItem.getQuantity())))
                .lot(salesItem.getLot())
                .build();
    }

    public static @NotNull List<FranchiseSalesProductResponse> from(List<SalesItem> salesItems) {
        return salesItems.stream()
                .map(FranchiseSalesProductResponse::from)
                .toList();
    }
}
