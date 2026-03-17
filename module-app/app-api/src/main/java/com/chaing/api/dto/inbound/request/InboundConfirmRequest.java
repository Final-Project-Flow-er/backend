package com.chaing.api.dto.inbound.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InboundConfirmRequest(
        @NotEmpty(message = "선택된 제품이 존재하지 않습니다.")
        List<String> serialCodes
) {
}
