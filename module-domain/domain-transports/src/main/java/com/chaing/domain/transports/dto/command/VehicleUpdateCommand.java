package com.chaing.domain.transports.dto.command;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;

public record VehicleUpdateCommand(

                Long transportId,
                String vehicleNumber,
                VehicleType vehicleType,
                String driverName,
                String driverPhone,
                Long maxLoad,
                Dispatchable dispatchable,
                UsableStatus status
) {
}
