package com.chaing.api.dto.transport.management.request;

import com.chaing.core.enums.UsableStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTransportStatusRequest(

        @NotNull(message = "변경할 상태값은 필수입니다.")
        UsableStatus status
) {
}
