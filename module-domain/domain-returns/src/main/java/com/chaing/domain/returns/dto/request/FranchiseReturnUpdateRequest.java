package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FranchiseReturnUpdateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String serialCode,

        @NotBlank
        String returnCode,

        @NotNull
        Long orderItemId
) {
}
