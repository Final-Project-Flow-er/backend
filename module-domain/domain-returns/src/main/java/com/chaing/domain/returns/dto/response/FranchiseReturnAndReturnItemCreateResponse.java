package com.chaing.domain.returns.dto.response;

import java.util.List;

public record FranchiseReturnAndReturnItemCreateResponse(
        ReturnInfo returnInfo,

        List<ReturnAndOrderInfo> returnAndOrderInfos
) {
}
