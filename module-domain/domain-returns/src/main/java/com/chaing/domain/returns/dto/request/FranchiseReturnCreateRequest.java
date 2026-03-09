package com.chaing.domain.returns.dto.request;

import com.chaing.domain.returns.enums.ReturnType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record FranchiseReturnCreateRequest(
        @NotBlank
        String orderCode,

        String username,

        String phoneNumber,

        String franchiseCode,

        @NotNull
        ReturnType returnType,

        String description,

        Integer quantity,

        BigDecimal totalPrice,

        List<FranchiseReturnItemCreateRequest> items
) {
}
