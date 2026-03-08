package com.chaing.api.dto.outbound.request;

import java.util.List;

public record OutboundAssignRequest(
        String boxCode,
        List<String> serialCodes
) {
}
