package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.Sales;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseSalesCancellationResponse(
        @NotBlank
        String salesCode,

        @NotNull
        BigDecimal totalPrice
) {
    public static FranchiseSalesCancellationResponse from(Sales sales) {
        return FranchiseSalesCancellationResponse.builder()
                .salesCode(sales.getSalesCode())
                .totalPrice(sales.getTotalAmount())
                .build();
    }
}
