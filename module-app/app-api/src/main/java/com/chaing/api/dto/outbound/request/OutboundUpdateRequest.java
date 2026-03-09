package com.chaing.api.dto.outbound.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OutboundUpdateRequest(
        @NotEmpty(message = "선택된 제품이 존재하지 않습니다.")
        List<@NotBlank String> serialCodes
) {
}
