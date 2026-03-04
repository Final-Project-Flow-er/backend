package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.dto.command.ReturnItemInspection;
import lombok.Builder;

import java.util.Map;

@Builder
public record HQReturnUpdateResponse(
        Long returnId,

        String returnCode,

        Map<String, ReturnItemInspection> inspectionBySerialCode
) {
}
