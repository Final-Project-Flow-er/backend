package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record HQReturnDetailResponse(
        String returnCode,

        String orderCode,

        String franchiseCode,

        LocalDateTime requestedDate,

        String username,

        String phoneNumber,

        ReturnType type,

        String description,

        BigDecimal totalAmount,

        List<FranchiseReturnItemResponse> items
) {
}
