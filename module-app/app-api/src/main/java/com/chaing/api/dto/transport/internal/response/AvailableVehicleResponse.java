package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.transports.entity.Vehicle;
import lombok.Builder;

@Builder
public record AvailableVehicleResponse(
        Long vehicleId,         // 차량 고유 ID
        String vehicleNumber,   // 차량 번호
        Long maxLoad,           // 최대 적재량
        Long currentLoad,       // 현재 적재량
        Long availableLoad      // 적재 가능 잔여량 (max - current)
) {
    public static AvailableVehicleResponse from(Vehicle vehicle, Long currentLoad) {
            long safeCurrentLoad = currentLoad == null ? 0L : currentLoad;
            long safeMaxLoad = vehicle.getMaxLoad() == null ? 0L : vehicle.getMaxLoad();
            long safeAvailableLoad = Math.max(0L, safeMaxLoad - safeCurrentLoad);

        return AvailableVehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .maxLoad(safeMaxLoad)
                .currentLoad(safeCurrentLoad)
                .availableLoad(safeAvailableLoad)
                .build();
    }
}