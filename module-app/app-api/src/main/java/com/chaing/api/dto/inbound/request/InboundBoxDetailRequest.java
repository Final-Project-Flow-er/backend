package com.chaing.api.dto.inbound.request;

import jakarta.validation.constraints.NotBlank;

public record InboundBoxDetailRequest(
        @NotBlank String boxCode
) {
}
