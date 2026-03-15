package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.SalesItem;
import com.chaing.domain.sales.exception.FranchiseSalesErrorCode;
import com.chaing.domain.sales.exception.FranchiseSalesException;
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
    public static FranchiseSalesProductResponse from(SalesItem salesItem, Integer quantity) {
        return FranchiseSalesProductResponse.builder()
                .productCode(salesItem.getProductCode())
                .productName(salesItem.getProductName())
                .quantity(quantity)
                .unitPrice(salesItem.getUnitPrice())
                .totalPrice(salesItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)))
                .lot(salesItem.getLot())
                .build();
    }

    public static @NotNull List<FranchiseSalesProductResponse> from(List<SalesItem> salesItems) {

        if(salesItems == null || salesItems.isEmpty()) {
            throw new FranchiseSalesException(FranchiseSalesErrorCode.SALES_NOT_FOUND);
        }

        long countQuantity = salesItems.stream().distinct().count();
        Integer quantity = Math.toIntExact(countQuantity);

        return salesItems.stream()
                .map(salesItem -> {
                    return FranchiseSalesProductResponse.from(salesItem, quantity);
                })
                .toList();
    }
}
