package com.chaing.domain.transports.dto.condition;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;

public record TransportSearchCondition(

        String companyName,
        String manager,
        Long unitPrice,
        Region usableRegion,
        UsableStatus status
) {
}
