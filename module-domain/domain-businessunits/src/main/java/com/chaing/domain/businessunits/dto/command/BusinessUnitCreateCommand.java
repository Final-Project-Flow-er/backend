package com.chaing.domain.businessunits.dto.command;

import com.chaing.core.enums.Region;

import java.time.LocalTime;

public record BusinessUnitCreateCommand(

        String name,
        String address,
        String phone,
        String representativeName,
        String businessNumber,
        Region region,

        FranchiseCreate franchiseCreate,
        FactoryCreate factoryCreate
) {

    public record FranchiseCreate(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime
    ) {
    }

    public record FactoryCreate(
            int productionLineCount
    ) {
    }
}
