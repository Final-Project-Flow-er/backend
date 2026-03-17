package com.chaing.domain.transports.dto.command;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;

import java.time.LocalDate;

public record TransportUpdateCommand(

                String companyName,
                String manager,
                String officePhone,
                String address,
                Integer ownedVehicles,
                Long unitPrice,
                LocalDate contractStartDate,
                LocalDate contractEndDate,
                Region usableRegion,
                UsableStatus status
) {
}
