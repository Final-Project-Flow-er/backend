package com.chaing.api.dto.transport.internal.request;

import com.chaing.domain.transports.enums.DeliverStatus;

public record TransportForceUpdateRequest(
        DeliverStatus targetStatus
) {
}
