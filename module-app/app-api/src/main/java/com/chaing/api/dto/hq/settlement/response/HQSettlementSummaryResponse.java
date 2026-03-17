package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record HQSettlementSummaryResponse(
                @Schema(description = "최종 정산 금액", example = "100000000") BigDecimal finalAmount,

                @Schema(description = "발주 매출", example = "50000000") BigDecimal orderAmount,

                @Schema(description = "매출 합계", example = "80000000") BigDecimal saleAmount,

                @Schema(description = "수수료 수익", example = "15000000") BigDecimal commissionFee,

                @Schema(description = "배달비 수익", example = "5000000") BigDecimal deliveryFee,

                @Schema(description = "반품 차감액", example = "2000000") BigDecimal refundAmount,

                @Schema(description = "본사 손실", example = "500000") BigDecimal lossAmount) {
        public static HQSettlementSummaryResponse of(BigDecimal finalAmount, BigDecimal orderAmount,
                        BigDecimal saleAmount,
                        BigDecimal commissionFee, BigDecimal deliveryFee, BigDecimal refundAmount,
                        BigDecimal lossAmount) {
                return HQSettlementSummaryResponse.builder()
                                .finalAmount(finalAmount)
                                .orderAmount(orderAmount)
                                .saleAmount(saleAmount)
                                .commissionFee(commissionFee)
                                .deliveryFee(deliveryFee)
                                .refundAmount(refundAmount)
                                .lossAmount(lossAmount)
                                .build();
        }
}
