package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseReturnTargetOrderItem(
        String boxCode,

        String productCode,

        String productName,

        BigDecimal unitPrice
) {
}
