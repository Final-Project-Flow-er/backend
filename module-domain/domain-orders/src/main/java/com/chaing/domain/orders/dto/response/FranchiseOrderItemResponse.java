package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.entity.FranchiseOrderItem;
import jakarta.validation.Valid;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FranchiseOrderItemResponse(
        String serialCode,

        BigDecimal unitPrice,

        Integer quantity,

        BigDecimal totalUnitPrice
) {
    public static FranchiseOrderItemResponse from(FranchiseOrderItem franchiseOrderItem) {
        return FranchiseOrderItemResponse.builder()
                .serialCode(franchiseOrderItem.getSerialCode())
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
