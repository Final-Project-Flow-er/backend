package com.chaing.api.dto.transport.management.request;

import com.chaing.domain.transports.dto.command.VehicleUpdateCommand;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;

public record UpdateVehicleRequest(

        Long transportId,
        String vehicleNumber,
        VehicleType vehicleType,
        String driverName,
        String driverPhone,
        Long maxLoad,
        Dispatchable dispatchable
) {
    public VehicleUpdateCommand toCommand() {
        return new VehicleUpdateCommand(
                this.transportId,
                this.vehicleNumber,
                this.vehicleType,
                this.driverName,
                this.driverPhone,
                this.maxLoad,
                this.dispatchable
        );
    }
}
