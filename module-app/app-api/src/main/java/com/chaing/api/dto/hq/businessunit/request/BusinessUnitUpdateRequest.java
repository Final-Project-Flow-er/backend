package com.chaing.api.dto.hq.businessunit.request;

import com.chaing.core.enums.Region;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand.HqUpdate;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand.FranchiseUpdate;
import com.chaing.domain.businessunits.dto.command.BusinessUnitUpdateCommand.FactoryUpdate;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record BusinessUnitUpdateRequest(
        String name,
        String address,
        String phone,
        String representativeName,
        Region region,

        HqUpdateRequest hqUpdate,
        FranchiseUpdateRequest franchiseUpdate,
        FactoryUpdateRequest factoryUpdate
) {

    public BusinessUnitUpdateCommand toCommand() {
        return new BusinessUnitUpdateCommand(
                this.name,
                this.address,
                this.phone,
                this.representativeName,
                this.region,
                this.hqUpdate != null ? this.hqUpdate.toCommand() : null,
                this.franchiseUpdate != null ? this.franchiseUpdate.toCommand() : null,
                this.factoryUpdate != null ? this.factoryUpdate.toCommand() : null
        );
    }

    public record HqUpdateRequest() {
        public HqUpdate toCommand() {
            return new HqUpdate();
        }
    }

    public record FranchiseUpdateRequest(
            String operatingDays,
            LocalTime openTime,
            LocalTime closeTime,
            String imageUrl,
            Integer warningCount,
            LocalDateTime penaltyEndDate,
            Double distanceToFactory
    ) {
        public FranchiseUpdate toCommand() {
            return new FranchiseUpdate(
                    operatingDays, openTime, closeTime, imageUrl,
                    warningCount, penaltyEndDate, distanceToFactory
            );
        }
    }

    public record FactoryUpdateRequest(
            Integer productionLineCount
    ) {
        public FactoryUpdate toCommand() {
            return new FactoryUpdate(productionLineCount);
        }
    }
}
