package com.chaing.domain.inventories.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DisposalRequest(
        @NotBlank
        String actorType,
        Long actorId,
        @NotEmpty
        List<Long> inventoryIds
) {
}
