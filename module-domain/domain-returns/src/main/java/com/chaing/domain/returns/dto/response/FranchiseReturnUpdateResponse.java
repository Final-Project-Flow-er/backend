package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseReturnUpdateResponse(
        String returnCode,

        ReturnStatus status,

        Long franchiseOrderId,

        ReturnType type,

        LocalDateTime requestedDate,

        List<FranchiseReturnProductInfo> items
) {
}
