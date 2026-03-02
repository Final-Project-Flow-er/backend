package com.chaing.domain.businessunits.dto.command;

import com.chaing.core.enums.Region;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record BusinessUnitUpdateCommand(

        String name,
        String address,
        String phone,
        String representativeName,
        Region region,

        HqUpdate hqUpdate,
        FranchiseUpdate franchiseUpdate,
        FactoryUpdate factoryUpdate
) {
    public record HqUpdate() {}

    public record FranchiseUpdate(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime,
            String imageUrl,
            Integer warningCount,
            LocalDateTime penaltyEndDate,
            Double distanceToFactory
    ) {}

    public record FactoryUpdate(
            int productionLineCount
    ) {}
}
