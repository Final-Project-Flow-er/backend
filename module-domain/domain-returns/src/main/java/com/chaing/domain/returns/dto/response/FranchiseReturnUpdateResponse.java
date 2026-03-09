package com.chaing.domain.returns.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseReturnUpdateResponse(
        String returnCode,

        String orderCode,

        List<FranchiseReturnItemResponse> items
) {
}
