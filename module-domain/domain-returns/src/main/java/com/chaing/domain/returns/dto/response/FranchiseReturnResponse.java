package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseReturnResponse(
        String returnCode,

        ReturnStatus status,

        String orderCode,

        String productCode,

        String productName,

        BigDecimal unitPrice,

        Integer quantity,

        BigDecimal totalPrice,

        ReturnType type,

        LocalDateTime requestedDate
) {
}
