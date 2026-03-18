package com.chaing.api.dto.outbound.request;

import java.util.List;

public record OutboundStatusUpdateRequest(
        List<String> serialCodes
) {
}
