package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.condition.TransportSearchCondition;

public record TransportSearchRequest(

        String companyName,
        String manager,
        Long unitPrice,
        Region usableRegion,
        UsableStatus status
) {
    public TransportSearchCondition toCondition() {
        return new TransportSearchCondition(
                companyName,
                manager,
                unitPrice,
                usableRegion,
                status
        );
    }
}
