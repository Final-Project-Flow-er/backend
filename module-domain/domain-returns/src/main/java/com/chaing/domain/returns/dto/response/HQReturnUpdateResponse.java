package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import com.chaing.domain.returns.enums.ReturnStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record HQReturnUpdateResponse(
        String returnCode,

        ReturnStatus status,

        List<ReturnItemInspection> returnItemInspection
) {
}
