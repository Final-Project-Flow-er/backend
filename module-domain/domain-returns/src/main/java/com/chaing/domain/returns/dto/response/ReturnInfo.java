package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.entity.Returns;
import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReturnInfo(
        String returnCode,

        ReturnStatus status,

        Long franchiseOrderId,

        ReturnType type,

        LocalDateTime requestedDate
) {
    public static ReturnInfo from(Returns returns) {
        return ReturnInfo.builder()
                .returnCode(returns.getReturnCode())
                .status(returns.getReturnStatus())
                .franchiseOrderId(returns.getFranchiseOrderId())
                .type(returns.getReturnType())
                .requestedDate(returns.getCreatedAt())
                .build();
    }
}
