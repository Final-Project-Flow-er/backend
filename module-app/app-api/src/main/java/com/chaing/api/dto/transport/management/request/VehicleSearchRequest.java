package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.condition.VehicleSearchCondition;
import com.chaing.domain.transports.enums.Dispatchable;
import com.chaing.domain.transports.enums.VehicleType;

public record VehicleSearchRequest(

                Long transportId,
                String companyName,
                String vehicleNumber,
                VehicleType vehicleType,
                Long maxLoad,
                Dispatchable dispatchable,
                UsableStatus status
) {
    public VehicleSearchCondition toCondition() {
        return new VehicleSearchCondition(
                transportId,
                companyName,
                vehicleNumber,
                vehicleType,
                maxLoad,
                dispatchable,
                status
        );
    }
}
