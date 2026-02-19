package com.chaing.domain.sales.dto.info;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record FranchiseSalesInfoResponse(
        String salesCode, //

        LocalDateTime salesDate, //

        String productCode,//

        String productName,//

        Integer quantity,//

        BigDecimal unitPrice,//

        BigDecimal totalPrice //
) {
}
