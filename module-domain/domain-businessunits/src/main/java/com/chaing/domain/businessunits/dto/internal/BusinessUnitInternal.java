package com.chaing.domain.businessunits.dto.internal;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.businessunits.entity.Factory;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.entity.Headquarter;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record BusinessUnitInternal(

        Long id,
        String code,
        String name,
        String address,
        String phone,
        String representativeName,
        String businessNumber,
        Region region,
        UsableStatus status,
        String unitType,

        HeadquarterDetail hqDetail,
        FranchiseDetail franchiseDetail,
        FactoryDetail factoryDetail
) {
    public record HeadquarterDetail() {}

    public record FranchiseDetail(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime,
            int warningCount,
            LocalDateTime penaltyEndDate,
            boolean isReturnBlocked,
            Double distanceToFactory
    ) {}

    public record FactoryDetail(
            int productionLineCount
    ) {}

    public static BusinessUnitInternal from(Headquarter hq) {
        return new BusinessUnitInternal(
                hq.getHqId(), hq.getHqCode(), hq.getName(), hq.getAddress(), hq.getPhone(),
                hq.getRepresentativeName(), hq.getBusinessNumber(), null, UsableStatus.ACTIVE, "HQ",
                new HeadquarterDetail(), null, null
        );
    }

    public static BusinessUnitInternal from(Franchise fr) {
        return new BusinessUnitInternal(
                fr.getFranchiseId(), fr.getFranchiseCode(), fr.getName(), fr.getAddress(), fr.getPhone(),
                fr.getRepresentativeName(), fr.getBusinessNumber(), fr.getRegion(), fr.getStatus(), "FRANCHISE",
                null,
                new FranchiseDetail(fr.getOperatingDays(), fr.getOpenTime(), fr.getCloseTime(),
                        fr.getWarningCount(), fr.getPenaltyEndDate(), fr.isReturnBlocked(), fr.getDistanceToFactory()),
                null
        );
    }

    public static BusinessUnitInternal from(Factory fc) {
        return new BusinessUnitInternal(
                fc.getFactoryId(), fc.getFactoryCode(), fc.getName(), fc.getAddress(), fc.getPhone(),
                fc.getRepresentativeName(), fc.getBusinessNumber(), fc.getRegion(), fc.getStatus(), "FACTORY",
                null, null, new FactoryDetail(fc.getProductionLineCount())
        );
    }
}
