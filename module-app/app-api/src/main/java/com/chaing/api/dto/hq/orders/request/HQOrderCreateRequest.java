package com.chaing.api.dto.hq.orders.request;

import com.chaing.domain.orders.dto.reqeust.HQOrderItemCreateInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

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

        @NotNull
        List<HQOrderItemCreateInfo> items
) {
}
