package com.chaing.api.dto.franchise.sales.response;

import com.chaing.domain.sales.dto.response.FranchiseSalesInfoResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FranchiseSalesResponse(
        String salesCode,

        LocalDateTime salesDate,

        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

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
