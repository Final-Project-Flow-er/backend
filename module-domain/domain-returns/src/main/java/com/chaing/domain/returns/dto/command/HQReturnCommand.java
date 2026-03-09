package com.chaing.domain.returns.dto.command;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record HQReturnCommand(
        Long returnId,

        Long franchiseId,

        LocalDateTime requestedDate,

        String returnCode,

        ReturnStatus status,

        ReturnType type,

        Integer quantity,

        BigDecimal totalPrice,

        Long userId
) {
    public static HQReturnCommand from(Returns returns) {
        return HQReturnCommand.builder()
                .returnId(returns.getReturnId())
                .franchiseId(returns.getFranchiseId())
                .requestedDate(returns.getCreatedAt())
                .returnCode(returns.getReturnCode())
                .status(returns.getReturnStatus())
                .type(returns.getReturnType())
                .quantity(returns.getTotalReturnQuantity())
                .totalPrice(returns.getTotalReturnAmount())
                .userId(returns.getUserId())
                .build();
    }
}
