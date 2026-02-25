package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FranchiseReturnAndReturnItemResponse(
        String returnCode,

        ReturnStatus status,

        Long franchiseOrderId,

        ReturnType type,

        LocalDateTime requestedDate,

        Long franchiseOrderItemId
) {
    @QueryProjection
    public FranchiseReturnAndReturnItemResponse {}
}
