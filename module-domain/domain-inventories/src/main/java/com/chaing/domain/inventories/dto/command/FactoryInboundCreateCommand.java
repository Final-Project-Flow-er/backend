package com.chaing.domain.inventories.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FactoryInboundCreateCommand(
        @NotBlank String serialCode,
        @NotNull Long productId,
        @NotNull LocalDate manufactureDate
) {}
