package com.chaing.domain.transports.dto.condition;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;

public record VehicleSearchCondition(

        Long transportId,
        String vehicleNumber,
        VehicleType vehicleType,
        Long maxLoad,
        Dispatchable dispatchable,
        UsableStatus status) {
}
