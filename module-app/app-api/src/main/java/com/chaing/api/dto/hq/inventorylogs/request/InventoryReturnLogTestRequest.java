package com.chaing.api.dto.hq.inventorylogs.request;


import com.chaing.core.enums.LogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryReturnLogTestRequest(
        @NotNull Long returnId,
        @NotNull LogType logType,
        @NotBlank String actorType,
        @NotNull Long actorId
) {}