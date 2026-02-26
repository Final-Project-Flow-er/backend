package com.chaing.domain.returns.dto.response;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;

public record FranchiseReturnCreateResponse(
        FranchiseOrderInfo orderInfo,

        List<FranchiseReturnTargetOrderItem> items
) {
}
