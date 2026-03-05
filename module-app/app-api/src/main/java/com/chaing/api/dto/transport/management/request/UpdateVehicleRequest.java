package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.enums.Dispatchable;

public record UpdateVehicleRequest(

        Long transportId,
        String vehicleNumber,
        String vehicleType,
        String driverName,
        String driverPhone,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus usableStatus
) {
    public VehicleUpdateCommand toCommand() {
        return new VehicleUpdateCommand(
                this.transportId,
                this.vehicleNumber,
                this.vehicleType,
                this.driverName,
                this.driverPhone,
                this.maxLoad,
                this.dispatchable,
                this.usableStatus
        );
    }
}
