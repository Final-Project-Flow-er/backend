package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.transports.dto.info.TransportLogInfo;
import com.chaing.domain.transports.enums.DeliverStatus;
import com.chaing.domain.transports.enums.VehicleType;

import java.time.LocalDateTime;

public record TransportLogResponse(
        Long transportLogId,
        String orderCode,
        String returnCode,
        String franchiseName,
        DeliverStatus deliverStatus,
        VehicleType vehicleType,
        String driverName,
        String vehicleNumber,
        String trackingNumber,
        Long weight,
        LocalDateTime createdAt
) {
    public static TransportLogResponse from(TransportLogInfo logInfo, String franchiseName) {
        return new TransportLogResponse(
                logInfo.transportLogId(),
                logInfo.orderCode(),
                logInfo.returnCode(),
                franchiseName,
                logInfo.deliverStatus(),
                logInfo.vehicleType(),
                logInfo.driverName(),
                logInfo.vehicleNumber(),
                logInfo.trackingNumber(),
                logInfo.weight(),
                logInfo.createdAt()
        );
    }
}
