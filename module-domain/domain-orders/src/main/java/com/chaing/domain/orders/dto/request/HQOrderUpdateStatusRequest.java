package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record HQOrderUpdateStatusRequest(
        @NotNull
        List<String> orderCodes,

        @NotNull
        Boolean isAccepted
) {
}
