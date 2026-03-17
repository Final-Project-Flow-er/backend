package com.chaing.api.dto.transport.management.response;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;
import lombok.Builder;

@Builder
public record VehicleDetailResponse(

        Long id,
        Long transportId,
        String vehicleNumber,
        VehicleType vehicleType,
        String driverName,
        String driverPhone,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus status
) {
    public static VehicleDetailResponse from(Vehicle vehicle) {
        return new VehicleDetailResponse(
                vehicle.getVehicleId(),
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
