package com.chaing.api.dto.transport.management.response;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import lombok.Builder;

@Builder
public record VehicleDetailResponse(

        Long transportId,
        String vehicleNumber,
        String vehicleType,
        String driverName,
        String driverPhone,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus status
) {
    public static VehicleDetailResponse from(Vehicle vehicle) {
        return new VehicleDetailResponse(
                vehicle.getTransportId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getDriverName(),
                vehicle.getDriverPhone(),
                vehicle.getMaxLoad(),
                vehicle.getDispatchable(),
                vehicle.getStatus()
        );
    }
}
