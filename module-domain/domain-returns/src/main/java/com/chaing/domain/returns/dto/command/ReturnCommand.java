package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ReturnCommand(
        Long returnId,

        Long orderId,

        String returnCode,

        Long userId,

        ReturnType type,

        String description,

        Integer quantity,

        BigDecimal totalAmount,

        ReturnStatus status,

        LocalDateTime requestedAt
) {
    @QueryProjection
    public ReturnCommand {}

    public static ReturnCommand from(Returns returns) {
        return ReturnCommand.builder()
                .returnId(returns.getReturnId())
                .orderId(returns.getFranchiseOrderId())
                .returnCode(returns.getReturnCode())
                .userId(returns.getUserId())
                .type(returns.getReturnType())
                .description(returns.getDescription())
                .quantity(returns.getTotalReturnQuantity())
                .totalAmount(returns.getTotalReturnAmount())
                .status(returns.getReturnStatus())
                .requestedAt(returns.getCreatedAt())
                .build();
    }
}
