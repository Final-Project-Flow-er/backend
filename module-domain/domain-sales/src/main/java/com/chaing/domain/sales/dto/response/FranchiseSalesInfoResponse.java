package com.chaing.domain.sales.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseSalesInfoResponse(
        String salesCode,

        LocalDateTime salesDate,

        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

        Boolean isCanceled
) {
    @QueryProjection
    public FranchiseSalesInfoResponse {}
}
