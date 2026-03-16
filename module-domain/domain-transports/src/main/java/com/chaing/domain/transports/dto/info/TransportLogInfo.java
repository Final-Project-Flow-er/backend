package com.chaing.domain.transports.dto.info;

import com.chaing.domain.transports.entity.TransportLog;
import com.chaing.domain.transports.entity.Vehicle;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.VehicleType;

import java.util.List;

public record TransportLogInfo(
        Long transportLogId,
        String orderCode,
        String returnCode,
        Long franchiseId,
        DeliverStatus deliverStatus,
        VehicleType vehicleType,
        String driverName,
        String vehicleNumber,
        String trackingNumber,
        Long weight
) {
    public static TransportLogInfo create(TransportLog logInfo, Vehicle vehicle) {
        return new TransportLogInfo(
                logInfo.getTransportLogId(),
                logInfo.getOrderCode(),
                logInfo.getReturnCode(),
                logInfo.getFranchiseId(),
                logInfo.getDeliverStatus(),
                vehicle.getVehicleType(),
                vehicle.getDriverName(),
                vehicle.getVehicleNumber(),
                logInfo.getTrackingNumber(),
                logInfo.getWeight()
        );
    }
}
