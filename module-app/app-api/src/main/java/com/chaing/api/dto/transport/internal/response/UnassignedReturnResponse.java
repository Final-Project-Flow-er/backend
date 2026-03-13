package com.chaing.api.dto.transport.internal.response;

import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.returns.dto.command.HQReturnCommand;

import java.time.LocalDateTime;

public record UnassignedReturnResponse(
        Long returnId,
        String returnCode,
        String franchiseName,
        String address,
        LocalDateTime requestedDate
        ) {
    public static UnassignedReturnResponse from(
            HQReturnCommand returnInfo, BusinessUnitInternal franchiseInfo
    ) {
        return new UnassignedReturnResponse(
                returnInfo.returnId(),
                returnInfo.returnCode(),
                franchiseInfo.name(),
                franchiseInfo.address(),
                returnInfo.requestedDate()
        );
    }
}
