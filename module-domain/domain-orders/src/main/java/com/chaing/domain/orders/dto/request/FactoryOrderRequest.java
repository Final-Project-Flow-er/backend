package com.chaing.domain.orders.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FactoryOrderRequest(
        @NotNull
        @NotEmpty
        List<@NotBlank String> orderCodes
) {
}
