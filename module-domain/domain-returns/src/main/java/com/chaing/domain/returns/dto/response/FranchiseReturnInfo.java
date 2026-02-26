package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;

import java.time.LocalDateTime;

public record FranchiseReturnInfo(
        String returnCode,

        Long orderId,

        String username,

        String phoneNumber,

        LocalDateTime requestedDate,

        ReturnStatus status,

        ReturnType type,

        String description
) {
    public static FranchiseReturnInfo from(Returns returns) {
        return new FranchiseReturnInfo(
                returns.getReturnCode(),
                returns.getFranchiseOrderId(),
                returns.getUsername(),
                returns.getPhoneNumber(),
                returns.getCreatedAt(),
                returns.getReturnStatus(),
                returns.getReturnType(),
                returns.getDescription()
        );
    }
}
