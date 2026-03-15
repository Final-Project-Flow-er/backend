package com.chaing.api.dto.transport.internal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateDeliverStatusRequest(
        @NotEmpty List<@NotBlank String> orderCodes
) {
}
