package com.chaing.api.dto.transport.management.response;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
import lombok.Builder;

@Builder
public record TransportSummaryResponse(

        String companyName,
        String manager,
        String officePhone,
        Integer ownedVehicles,
        Long unitPrice,
        Region usableRegion,
        UsableStatus status
) {
    public static TransportSummaryResponse from(Transport transport) {
        return new TransportSummaryResponse(
                transport.getCompanyName(),
                transport.getManager(),
                transport.getOfficePhone(),
                transport.getOwnedVehicles(),
                transport.getUnitPrice(),
                transport.getUsableRegion(),
                transport.getStatus()
        );
    }
}
