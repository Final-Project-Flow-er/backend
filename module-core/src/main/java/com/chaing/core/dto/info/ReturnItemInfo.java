package com.chaing.core.dto.info;

import lombok.Builder;

@Builder
public record ReturnItemInfo(
        String boxCode,

        Long productId
) {
}
