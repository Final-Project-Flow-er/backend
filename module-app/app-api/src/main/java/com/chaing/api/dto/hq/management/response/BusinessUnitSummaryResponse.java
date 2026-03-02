package com.chaing.api.dto.hq.management.response;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import lombok.Builder;

@Builder
public record BusinessUnitSummaryResponse(

        Long id,
        String code,
        String name,
        String representativeName,
        Region region,
        UsableStatus status,
        String unitType,
        String operatingDays,
        boolean isReturnBlocked
) {
    public static BusinessUnitSummaryResponse from(BusinessUnitInternal internal) {
        return new BusinessUnitSummaryResponse(
                internal.id(),
                internal.code(),
                internal.name(),
                internal.representativeName(),
                internal.region(),
                internal.status(),
                internal.unitType(),
                internal.franchiseDetail() != null ? internal.franchiseDetail().operatingDays() : null,
                internal.franchiseDetail() != null && internal.franchiseDetail().isReturnBlocked()
        );
    }
}
