package com.chaing.core.dto.info;

import com.chaing.core.enums.ReturnItemStatus;
import lombok.Builder;

@Builder
public record ReturnItemInspection(
        ReturnItemStatus status,

        String boxCode
) {
}
