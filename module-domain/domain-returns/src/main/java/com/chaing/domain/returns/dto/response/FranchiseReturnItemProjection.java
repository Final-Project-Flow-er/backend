package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;

import java.time.LocalDateTime;

public record FranchiseReturnItemProjection(
        String returnCode,
        ReturnStatus status,
        Long franchiseOrderId,
        Long franchiseOrderItemId,
        ReturnType type,
        LocalDateTime requestedDate
) {
}
