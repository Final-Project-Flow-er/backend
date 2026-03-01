package com.chaing.api.dto.transport.internal.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VehicleAssignmentRequest(
        @NotNull(message = "차량을 선택해주세요")
        Long vehicleId,
        @NotNull(message = "주문을 선택해주세요")
        List<Long> orderIds
) {
}
