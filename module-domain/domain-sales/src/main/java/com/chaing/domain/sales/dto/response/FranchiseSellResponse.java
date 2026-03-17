package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.Sales;
import com.chaing.domain.sales.entity.SalesItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseSellResponse(
        @NotBlank
        String salesCode,

        @NotNull
        Integer totalQuantity,

        @NotNull
        BigDecimal totalPrice,

        @NotNull
        List<FranchiseSellItemResponse> items
) {
    public static FranchiseSellResponse from(Sales sales, List<SalesItem> salesItems) {
        return FranchiseSellResponse.builder()
                .salesCode(sales.getSalesCode())
                .totalQuantity(sales.getQuantity())
                .totalPrice(sales.getTotalAmount())
                .items(FranchiseSellItemResponse.from(salesItems))
                .build();
    }
}
