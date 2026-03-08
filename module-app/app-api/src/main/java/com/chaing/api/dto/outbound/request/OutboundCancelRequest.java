package com.chaing.api.dto.outbound.request;

import java.util.List;

public record OutboundCancelRequest(
        String boxCode,
        List<String> serialCodes
) {
}
