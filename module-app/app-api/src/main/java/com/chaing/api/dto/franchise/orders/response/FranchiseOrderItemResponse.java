package com.chaing.api.dto.franchise.orders.response;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseOrderItemResponse(
        @NotNull
        Long productId,

        @NotNull
        @Min(0)
        BigDecimal unitPrice,

        @NotNull
        @Min(0)
        Integer quantity,

        @NotNull
        @Min(0)
        BigDecimal totalUnitPrice
) {
    public static FranchiseOrderItemResponse from(FranchiseOrderItem franchiseOrderItem) {
        return FranchiseOrderItemResponse.builder()
                .productId(franchiseOrderItem.getProductId())
                .unitPrice(franchiseOrderItem.getUnitPrice())
                .quantity(franchiseOrderItem.getQuantity())
                .totalUnitPrice(franchiseOrderItem.getUnitPrice().multiply(BigDecimal.valueOf(franchiseOrderItem.getQuantity())))
                .build();
    }

    public static @Valid List<FranchiseOrderItemResponse> from(List<FranchiseOrderItem> franchiseOrderItems) {
        return franchiseOrderItems.stream()
                .map(FranchiseOrderItemResponse::from)
                .toList();
    }
}
