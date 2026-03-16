package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HQReturnItemProjection(
        Long franchiseId,
        Long userId,
        LocalDateTime requestedDate,
        String returnCode,
        ReturnStatus status,
        ReturnType type,
        Integer quantity,
        BigDecimal totalPrice
) {
}
