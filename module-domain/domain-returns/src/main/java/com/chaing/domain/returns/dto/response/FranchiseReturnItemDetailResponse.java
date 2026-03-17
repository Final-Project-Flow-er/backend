package com.chaing.domain.returns.dto.response;

import com.chaing.core.enums.ReturnItemStatus;
import lombok.Builder;

@Builder
public record FranchiseReturnItemDetailResponse(
        String boxCode,

        String serialCode,

        Boolean isInspected,

        ReturnItemStatus status
) {
}
