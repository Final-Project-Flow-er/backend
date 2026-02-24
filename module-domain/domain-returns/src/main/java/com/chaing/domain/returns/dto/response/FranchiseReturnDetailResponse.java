package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseReturnDetailResponse(
        String returnCode,

        String orderCode,

        String franchiseCode,

        LocalDateTime requestedDate,

        ReturnStatus status,

        String username,

        String phoneNumber,

        ReturnType returnType,

        String description,

        List<FranchiseReturnItemResponse> items
) {
}
