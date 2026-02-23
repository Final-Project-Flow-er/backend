package com.chaing.api.dto.franchise.sales.response;

import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseSalesResponse(
        @NotBlank
        String salesCode,

        @NotNull
        LocalDateTime salesDate,

        @NotBlank
        String productCode,

        @NotBlank
        String productName,

        @NotNull
        @Min(1)
        Integer quantity,

        @NotNull
        @Min(5000)
        BigDecimal unitPrice,

        @NotNull
        @Min(5000)
        BigDecimal totalPrice,

        @NotNull
        Boolean isCanceled
) {
    public static FranchiseSalesResponse from(FranchiseSalesInfoResponse salesInfo) {
        return new FranchiseSalesResponse(
                salesInfo.salesCode(),
                salesInfo.salesDate(),
                salesInfo.productCode(),
                salesInfo.productName(),
                salesInfo.quantity(),
                salesInfo.unitPrice(),
                salesInfo.totalPrice(),
                salesInfo.isCanceled()
        );
    }

    public static List<FranchiseSalesResponse> from(List<FranchiseSalesInfoResponse> salesInfos) {
        return salesInfos.stream()
                .map(FranchiseSalesResponse::from)
                .toList();
    }
}
