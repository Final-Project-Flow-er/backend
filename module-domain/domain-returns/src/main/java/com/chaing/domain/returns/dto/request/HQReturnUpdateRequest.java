package com.chaing.domain.returns.dto.request;

import com.chaing.domain.returns.enums.ReturnItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HQReturnUpdateRequest(
        @NotBlank
        String boxCode,

        @NotBlank
        String serialCode,

        @NotNull
        Boolean isInspected,

        @NotNull
        ReturnItemStatus status
) {
}
