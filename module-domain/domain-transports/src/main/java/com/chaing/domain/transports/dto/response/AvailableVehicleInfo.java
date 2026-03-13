package com.chaing.domain.transports.dto.response;

import com.chaing.domain.transports.entity.Vehicle;

public record AvailableVehicleInfo(
        String transportName,       // 업체명
        String driverName,          // 운전자명
        String driverPhoneNumber,   // 전화번호
        Long vehicleId,             // 차량 고유 ID
        String vehicleNumber,       // 차량 번호
        Long maxLoad,               // 최대 적재량
        Long currentLoad,           // 현재 적재량
        Long availableLoad          // 적재 가능 잔여량 (max - current)
) {
    public static AvailableVehicleInfo from(String transportName, Vehicle vehicle, Long currentWeight) {
        long safeCurrentWeight = currentWeight == null ? 0 : currentWeight;
        long safeMaxLoad = vehicle.getMaxLoad() == null ? 0 : vehicle.getMaxLoad();

        return new AvailableVehicleInfo(
                transportName,
                vehicle.getDriverName(),
                vehicle.getDriverPhone(),
                vehicle.getVehicleId(),
                vehicle.getVehicleNumber(),
                safeMaxLoad,
                safeCurrentWeight,
                Math.max(0L, safeMaxLoad - safeCurrentWeight)
        );
    }
}
