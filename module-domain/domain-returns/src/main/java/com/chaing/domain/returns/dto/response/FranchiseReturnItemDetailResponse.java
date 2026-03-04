package com.chaing.domain.returns.dto.response;

import com.chaing.domain.returns.enums.ReturnItemStatus;
import lombok.Builder;

@Builder
public record FranchiseReturnItemDetailResponse(
        String boxCode,

        String serialCode,

        Boolean isInspected,

        ReturnItemStatus status
) {
}
