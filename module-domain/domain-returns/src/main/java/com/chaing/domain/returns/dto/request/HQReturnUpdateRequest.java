package com.chaing.domain.returns.dto.request;

import com.chaing.domain.returns.enums.ReturnStatus;

import java.util.List;

public record HQReturnUpdateRequest(
        ReturnStatus returnStatus,

        List<HQReturnItemUpdateRequest> items
) {
}
