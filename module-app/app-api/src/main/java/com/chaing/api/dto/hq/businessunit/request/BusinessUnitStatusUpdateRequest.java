package com.chaing.api.dto.hq.businessunit.request;

import com.chaing.core.enums.UsableStatus;
import jakarta.validation.constraints.NotNull;

public record BusinessUnitStatusUpdateRequest(

        @NotNull(message = "변경할 상태값은 필수입니다.")
        UsableStatus status
) {
}
