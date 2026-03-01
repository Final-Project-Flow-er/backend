package com.chaing.api.dto.hq.orders.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HQOrderUpdateStatusRequest(
        @NotBlank
        List<String> orderCodes,

        @NotNull
        Boolean isAccepted
) {
}
