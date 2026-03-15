package com.chaing.api.dto.hq.businessunit.request;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.condition.BusinessUnitSearchCondition;

public record BusinessUnitSearchRequest(

        String code,
        String name,
        String representativeName,
        String businessNumber,
        Region region,
        UsableStatus status,

        String operatingDays,
        Boolean isReturnBlocked
) {
    public BusinessUnitSearchCondition toCondition() {
        return new BusinessUnitSearchCondition(
                code,
                name,
                representativeName,
                businessNumber,
                region,
                status,

                operatingDays,
                isReturnBlocked
        );
    }
}
