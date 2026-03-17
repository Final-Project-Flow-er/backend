package com.chaing.core.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FranchiseReturnUpdateRequest(
        @NotBlank
        String boxCode
) {
}
