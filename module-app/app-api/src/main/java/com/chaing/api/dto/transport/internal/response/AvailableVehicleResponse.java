package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.transports.dto.response.AvailableVehicleInfo;
import com.chaing.domain.transports.entity.Vehicle;
import lombok.Builder;

@Builder
public record AvailableVehicleResponse(
        String transportName,       // 업체명
        String driverName,          // 운전자명
        String driverPhoneNumber,   // 전화번호
        Long vehicleId,             // 차량 고유 ID
        String vehicleNumber,       // 차량 번호
        Long maxLoad,               // 최대 적재량
        Long currentLoad,           // 현재 적재량
        Long availableLoad          // 적재 가능 잔여량 (max - current)
) {
    public static AvailableVehicleResponse from(AvailableVehicleInfo vehicleInfo) {
            long safeCurrentLoad = vehicleInfo.currentLoad() == null ? 0L : vehicleInfo.currentLoad();
            long safeMaxLoad = vehicleInfo.maxLoad() == null ? 0L : vehicleInfo.maxLoad();
            long safeAvailableLoad = Math.max(0L, safeMaxLoad - safeCurrentLoad);

        return AvailableVehicleResponse.builder()
                .transportName(vehicleInfo.transportName())
                .driverName(vehicleInfo.driverName())
                .driverPhoneNumber(vehicleInfo.driverPhoneNumber())
                .vehicleId(vehicleInfo.vehicleId())
                .vehicleNumber(vehicleInfo.vehicleNumber())
                .maxLoad(safeMaxLoad)
                .currentLoad(safeCurrentLoad)
                .availableLoad(safeAvailableLoad)
                .build();
    }
}