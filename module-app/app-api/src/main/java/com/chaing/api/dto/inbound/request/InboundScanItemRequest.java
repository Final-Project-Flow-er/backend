package com.chaing.api.dto.inbound.request;

import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record InboundScanItemRequest(
        @NotBlank String serialCode,
        @NotNull Long productId,
        @NotNull LocalDate manufactureDate
) {
    public static FactoryInboundCreateCommand from(InboundScanItemRequest request) {
        return new FactoryInboundCreateCommand(
                request.serialCode().trim().toUpperCase(),
                request.productId(),
                request.manufactureDate
        );
    }
}
