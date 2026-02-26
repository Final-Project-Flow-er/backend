package com.chaing.domain.returns.dto.response;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;

import java.util.List;

public record FranchiseReturnCreateResponse(
        FranchiseOrderInfo orderInfo,

        List<FranchiseReturnTargetOrderItem> items
) {
}
