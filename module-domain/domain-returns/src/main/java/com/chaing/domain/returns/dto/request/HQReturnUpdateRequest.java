package com.chaing.domain.returns.dto.request;

import com.chaing.domain.returns.enums.ReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HQReturnUpdateRequest(
        @NotNull
        ReturnStatus returnStatus,

        @NotEmpty
        @Valid
        List<HQReturnItemUpdateRequest> items
) {
}
