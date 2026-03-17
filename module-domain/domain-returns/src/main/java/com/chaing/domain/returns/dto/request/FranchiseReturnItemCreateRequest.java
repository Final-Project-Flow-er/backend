package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FranchiseReturnItemCreateRequest(
        @NotBlank
        String boxCode
) {
}
