package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.transports.dto.info.TransportLogInfo;
import com.chaing.domain.transports.entity.TransportLog;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.VehicleType;

public record TransportLogResponse(
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
    public static TransportLogResponse from(TransportLogInfo logInfo) {
        return new TransportLogResponse(
                logInfo.transportLogId(),
                logInfo.orderCode(),
                logInfo.returnCode(),
                logInfo.franchiseId(),
                logInfo.deliverStatus(),
                logInfo.vehicleType(),
                logInfo.driverName(),
                logInfo.vehicleNumber(),
                logInfo.trackingNumber(),
                logInfo.weight()
        );
    }
}
