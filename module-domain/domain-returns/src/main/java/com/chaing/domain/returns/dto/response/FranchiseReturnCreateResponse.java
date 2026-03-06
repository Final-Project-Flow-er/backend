package com.chaing.domain.returns.dto.response;

import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseReturnCreateResponse(
        FranchiseOrderInfo orderInfo,

        List<FranchiseReturnTargetOrderItem> items
) {
}
