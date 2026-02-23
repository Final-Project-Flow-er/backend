package com.chaing.api.dto.franchise.orders.response;

import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseOrderResponse(
        @NotBlank
        String orderCode,

        @NotNull
        FranchiseOrderStatus orderStatus,

        @NotNull
        @Min(0)
        BigDecimal totalPrice,

        @NotNull
        @PastOrPresent
        LocalDateTime createdAt,

        @NotBlank
        String receiver,

        @NotNull
        LocalDateTime deliveryDate,

        @Valid
        List<FranchiseOrderItemResponse> items
) {
    public static FranchiseOrderResponse from(FranchiseOrder franchiseOrder) {
        return FranchiseOrderResponse.builder()
                .orderCode(franchiseOrder.getOrderCode())
                .orderStatus(franchiseOrder.getOrderStatus())
                .totalPrice(franchiseOrder.getTotalAmount())
                .createdAt(franchiseOrder.getCreatedAt())
                .receiver(franchiseOrder.getUsername())
                .deliveryDate(franchiseOrder.getDeliveryDate())
                .items(FranchiseOrderItemResponse.from(franchiseOrder.getFranchiseOrderItems()))
                .build();
    }

    public static List<FranchiseOrderResponse> from(List<FranchiseOrder> orders) {
        return orders.stream()
                .map(FranchiseOrderResponse::from)
                .toList();
    }
}
