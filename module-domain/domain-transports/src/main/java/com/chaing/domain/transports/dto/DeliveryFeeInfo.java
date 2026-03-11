package com.chaing.domain.transports.dto;

import java.math.BigDecimal;

public record DeliveryFeeInfo(
        Long franchiseId,
        BigDecimal deliveryFee
) {
}
