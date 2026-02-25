package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FranchiseReturnUpdateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String serialCode,

        @NotBlank
        String returnCode,

        @NotBlank
        Long orderItemId
) {
}
