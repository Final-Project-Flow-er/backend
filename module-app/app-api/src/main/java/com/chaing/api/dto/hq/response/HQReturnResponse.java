package com.chaing.api.dto.hq.response;

import com.chaing.domain.returns.enums.ReturnStatus;
import com.chaing.domain.returns.enums.ReturnType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record HQReturnResponse(
        String franchiseCode,

        LocalDateTime requestedDate,

        String returnCode,

        ReturnStatus status,

        ReturnType type,

        Integer quantity,

        BigDecimal totalPrice,

        String receiver,

        String phoneNumber
) {
}
