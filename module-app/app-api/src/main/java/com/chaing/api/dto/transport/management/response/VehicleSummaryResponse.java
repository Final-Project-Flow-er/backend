package com.chaing.api.dto.transport.management.response;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.Dispatchable;
import lombok.Builder;

@Builder
public record VehicleSummaryResponse(

        Long transportId,
        String vehicleNumber,
        String vehicleType,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus status
) {
    public static VehicleSummaryResponse from(Vehicle vehicle) {
        return new VehicleSummaryResponse(
                vehicle.getTransportId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getMaxLoad(),
                vehicle.getDispatchable(),
                vehicle.getStatus()
        );
    }
}
