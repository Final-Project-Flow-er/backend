package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.dto.command.TransportUpdateCommand;

import java.time.LocalDate;

public record UpdateTransportRequest(

        String companyName,
        String manager,
        String officePhone,
        String address,
        Integer ownedVehicles,
        Long unitPrice,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        Region usableRegion,
        UsableStatus usableStatus
) {
    public TransportUpdateCommand toCommand() {
        return new TransportUpdateCommand(
                this.companyName,
                this.manager,
                this.officePhone,
                this.address,
                this.ownedVehicles,
                this.unitPrice,
                this.contractStartDate,
                this.contractEndDate,
                this.usableRegion,
                this.usableStatus
        );
    }
}
