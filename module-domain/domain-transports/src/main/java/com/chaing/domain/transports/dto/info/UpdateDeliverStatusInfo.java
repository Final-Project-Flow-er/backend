package com.chaing.domain.transports.dto.info;

import com.chaing.domain.transports.enums.DeliverStatus;

public record UpdateDeliverStatusInfo(
        String orderCode,
        String returnCode,
        DeliverStatus currentStatus
) {
    public static UpdateDeliverStatusInfo create(String orderCode, String returnCode, DeliverStatus currentStatus) {
        return new UpdateDeliverStatusInfo(orderCode, returnCode, currentStatus);
    }
}
