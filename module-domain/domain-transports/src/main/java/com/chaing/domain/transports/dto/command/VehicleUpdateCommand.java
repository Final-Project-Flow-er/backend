package com.chaing.domain.transports.dto.command;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.enums.Dispatchable;

public record VehicleUpdateCommand(

        Long transportId,
        String vehicleNumber,
        String vehicleType,
        String driverName,
        String driverPhone,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus usableStatus
) {
}
