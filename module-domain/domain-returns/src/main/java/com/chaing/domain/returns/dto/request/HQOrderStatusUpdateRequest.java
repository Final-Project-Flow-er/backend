package com.chaing.domain.returns.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HQOrderStatusUpdateRequest(
        @NotBlank
        String returnCode
) {
}
