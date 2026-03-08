package com.chaing.api.dto.inbound.request;

import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record InboundScanBoxRequest(
        @NotBlank String boxCode,
        @NotEmpty List<@NotBlank String> serialCodes,
        @NotNull Long productId,
        @NotNull LocalDate manufactureDate,
        @NotNull Long orderId,
        @NotEmpty List<@NotNull Long> orderItemIds
) {
    public static FranchiseInboundCreateCommand toCommand(InboundScanBoxRequest request, Long franchiseId) {
        return new FranchiseInboundCreateCommand(
                request.boxCode(),
                request.serialCodes().stream()
                        .map(code -> code.trim().toUpperCase())
                        .toList(),
                request.productId(),
                request.manufactureDate(),
                franchiseId,
                request.orderId(),
                request.orderItemIds()
        );
    }
}
