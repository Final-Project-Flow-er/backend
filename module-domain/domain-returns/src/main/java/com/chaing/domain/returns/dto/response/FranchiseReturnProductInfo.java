package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FranchiseReturnProductInfo(
        String boxCode,

        String serialCode,

        String productCode,

        String productName,

        BigDecimal unitPrice
) {
}
