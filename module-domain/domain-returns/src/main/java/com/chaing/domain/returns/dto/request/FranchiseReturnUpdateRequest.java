package com.chaing.api.dto.franchise.returns.request;

import jakarta.validation.constraints.NotBlank;

public record FranchiseReturnUpdateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String serialCode,

        @NotBlank
        String returnCode
) {
}
