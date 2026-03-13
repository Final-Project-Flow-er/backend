package com.chaing.api.dto.hq.products.response;

import lombok.Builder;

@Builder
public record HQComponentResponse(
        Long componentId,
        String name
) {
}
