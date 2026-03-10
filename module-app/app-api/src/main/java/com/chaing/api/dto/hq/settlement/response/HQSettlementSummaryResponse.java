package com.chaing.api.dto.hq.settlement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record HQSettlementSummaryResponse(
                @Schema(description = "최종 정산 금액", example = "100000000") Long finalAmount,

                @Schema(description = "발주 매출", example = "50000000") Long orderAmount,

                @Schema(description = "매출 합계", example = "80000000") Long saleAmount,

                @Schema(description = "수수료 수익", example = "15000000") Long commissionFee,

                @Schema(description = "배달비 수익", example = "5000000") Long deliveryFee,

                @Schema(description = "반품 차감액", example = "2000000") Long refundAmount,

                @Schema(description = "본사 손실", example = "500000") Long lossAmount) {
        public static HQSettlementSummaryResponse of(Long finalAmount, Long orderAmount, Long saleAmount,
                        Long commissionFee, Long deliveryFee, Long refundAmount, Long lossAmount) {
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
