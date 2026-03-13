package com.chaing.api.dto.hq.inventorylogs.request;


import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryOrderLogTestRequest(
        @NotNull Long orderId,
        @NotBlank String orderType,
        @NotNull Long fromId,
        @NotNull LogType logType,
        @NotBlank String actorType,
        @NotNull Long actorId
) {}
