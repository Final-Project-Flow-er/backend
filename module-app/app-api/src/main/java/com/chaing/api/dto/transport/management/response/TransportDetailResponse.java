package com.chaing.api.dto.transport.management.response;

import com.chaing.core.enums.Region;
import com.chaing.core.enums.UsableStatus;
import com.chaing.domain.transports.entity.Transport;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TransportDetailResponse(

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
    public static TransportDetailResponse from(Transport transport) {
        return new TransportDetailResponse(
                transport.getCompanyName(),
                transport.getManager(),
                transport.getOfficePhone(),
                transport.getAddress(),
                transport.getOwnedVehicles(),
                transport.getUnitPrice(),
                transport.getContractStartDate(),
                transport.getContractEndDate(),
                transport.getUsableRegion(),
                transport.getStatus()
        );
    }
}
