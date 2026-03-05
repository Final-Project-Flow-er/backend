package com.chaing.domain.inventories.dto.command;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record FranchiseInboundCreateCommand(
        String boxCode,
        List<String> serialCodes,
        Long productId,
        LocalDate manufactureDate,
        Long franchiseId
) {
}
