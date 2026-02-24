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

        BigDecimal unitPrice,

        Long franchiseOrderId,

        Integer quantity,

        ReturnType type,

        LocalDateTime requestedDate,



        String boxCode,

        String serialCode,

        String orderCode,

        String productCode,

        String productName,

        BigDecimal totalPrice
) {
}
