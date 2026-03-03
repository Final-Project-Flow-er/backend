package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record HQReturnDetailResponse(
        String returnCode,

        String orderCode,

        String franchiseCode,

        LocalDateTime requestedDate,

        String username,

        String phoneNumber,

        ReturnType type,

        ReturnStatus status,

        String description,

        BigDecimal totalAmount,

        List<FranchiseReturnItemResponse> items
) {
}
