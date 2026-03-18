package com.chaing.api.dto.outbound.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OutboundStatusUpdateRequest(
        @NotEmpty List<@NotBlank String> serialCodes
) {
}
