package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record HQReturnDetailCommand(
        Long returnId,

        Long franchiseOrderId,

        Long franchiseId,

        LocalDateTime requestedDate,

        String returnCode,

        ReturnStatus status,

        ReturnType type,

        Integer quantity,

        BigDecimal totalPrice,

        String receiver,

        String phoneNumber,

        String description
) {
    public static HQReturnDetailCommand from(Returns returns) {
        return HQReturnDetailCommand.builder()
                .returnId(returns.getReturnId())
                .franchiseOrderId(returns.getFranchiseOrderId())
                .franchiseId(returns.getFranchiseId())
                .requestedDate(returns.getCreatedAt())
                .returnCode(returns.getReturnCode())
                .status(returns.getReturnStatus())
                .type(returns.getReturnType())
                .quantity(returns.getTotalReturnQuantity())
                .totalPrice(returns.getTotalReturnAmount())
                .receiver(returns.getUsername())
                .phoneNumber(returns.getPhoneNumber())
                .description(returns.getDescription())
                .build();
    }
}