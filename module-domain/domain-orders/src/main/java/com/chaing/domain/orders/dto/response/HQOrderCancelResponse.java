package com.chaing.domain.orders.dto.response;

import com.chaing.domain.orders.enums.HQOrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record HQOrderCancelResponse(
        @NotBlank
        String orderCode,

        @NotBlank
        HQOrderStatus status
) {
}
