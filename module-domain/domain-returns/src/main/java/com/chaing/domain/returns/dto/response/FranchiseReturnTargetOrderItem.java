package com.chaing.domain.returns.dto.response;

import java.math.BigDecimal;

public record FranchiseReturnTargetOrderItem(
        String boxCode,

        String productCode,

        String productName,

        BigDecimal unitPrice
) {
}
