package com.chaing.domain.businessunits.dto.condition;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;

public record BusinessUnitSearchCondition(

        String code,
        String name,
        String representativeName,
        String businessNumber,
        Region region,
        UsableStatus status,

        String operatingDays,
        Boolean isReturnBlocked
) {
}
