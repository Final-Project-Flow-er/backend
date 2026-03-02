package com.chaing.api.dto.user.request;

import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest.FranchiseUpdateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest.FactoryUpdateRequest;

import java.time.LocalTime;

public record UpdateMyBusinessUnitInfoRequest(

        String phone,

        // 가맹점 전용
        String operatingDays,
        LocalTime openTime,
        LocalTime closeTime,
        String imageUrl,

        // 공장 전용
        Integer productionLineCount
) {
    public BusinessUnitUpdateRequest toManagementRequest() {
        return new BusinessUnitUpdateRequest(
                null,
                null,
                this.phone,
                null,
                null,
                null,

                new FranchiseUpdateRequest(
                        this.operatingDays,
                        this.openTime,
                        this.closeTime,
                        this.imageUrl,
                        null,
                        null,
                        null
                ),

                this.productionLineCount != null ? new FactoryUpdateRequest(this.productionLineCount) : null
        );
    }
}
