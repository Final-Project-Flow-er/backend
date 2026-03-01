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
        return AvailableVehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .maxLoad(vehicle.getMaxLoad())
                .currentLoad(currentLoad)
                // 화면에서 계산할 수도 있지만, 서버에서 넘겨주는 게 안전해!
                .availableLoad(vehicle.getMaxLoad() - currentLoad)
                .build();
    }
}