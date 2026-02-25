package com.chaing.api.dto.franchise.returns.request;

import com.chaing.domain.returns.dto.request.FranchiseReturnItemCreateRequest;
import com.chaing.domain.returns.enums.ReturnType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FranchiseReturnCreateRequest(
        @NotBlank
        String orderCode,

        @NotNull
        ReturnType returnType,

        String description,

        List<FranchiseReturnItemCreateRequest> items
) {
}
