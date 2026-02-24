package com.chaing.core.dto.returns.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReturnToInventoryRequest(
        @NotBlank
        String serialCode,

        @NotNull
        Long productId,

        @NotBlank
        String boxCode
) {
}
