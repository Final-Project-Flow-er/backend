package com.chaing.api.dto.hq.products.request;

import jakarta.validation.constraints.NotBlank;

public record HQComponentCreateRequest(
        @NotBlank String name
) {
}
