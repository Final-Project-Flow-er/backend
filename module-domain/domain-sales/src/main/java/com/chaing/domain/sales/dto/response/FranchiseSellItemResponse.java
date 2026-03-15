package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.SalesItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseSellItemResponse(
        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        BigDecimal unitPrice

) {
    public static FranchiseSellItemResponse from(SalesItem salesItem) {
        return FranchiseSellItemResponse.builder()
                .productCode(salesItem.getProductCode())
                .productName(salesItem.getProductName())
                .unitPrice(salesItem.getUnitPrice())
                .build();
    }

    public static @NotNull List<FranchiseSellItemResponse> from(List<SalesItem> salesItems) {
        return salesItems.stream()
                .map(FranchiseSellItemResponse::from)
                .toList();
    }
}
