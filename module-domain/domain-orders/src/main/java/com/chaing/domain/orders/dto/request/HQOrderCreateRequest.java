package com.chaing.domain.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record HQOrderCreateRequest(
        @NotBlank
        String username,

        @NotBlank
        String phoneNumber,

        String description,

        @NotNull
        Boolean isRegular,

        @NotNull
        LocalDateTime manufactureDate,

        @NotEmpty
        List<@Valid HQOrderItemCreateInfo> items
) {
}
