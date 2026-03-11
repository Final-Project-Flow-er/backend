package com.chaing.domain.transports.dto.response;

import com.chaing.domain.transports.entity.Vehicle;

public record AvailableVehicleInfo(
        Long vehicleId,
        String vehicleNumber, // 차량 번호
        Long maxLoad,       // 최대 적재량
        Long currentWeight,    // 현재 실려있는 양 (Transit 합계)
        Long availableWeight    // 남은
) {
    public static AvailableVehicleInfo from(Vehicle vehicle, Long currentWeight) {
        long safeCurrentWeight = currentWeight == null ? 0 : currentWeight;
        long safeMaxLoad = vehicle.getMaxLoad() == null ? 0 : vehicle.getMaxLoad();

        return new AvailableVehicleInfo(
                vehicle.getVehicleId(),
                vehicle.getVehicleNumber(),
                safeMaxLoad,
                safeCurrentWeight,
                Math.max(0L, safeMaxLoad - safeCurrentWeight)
        );
    }
}
