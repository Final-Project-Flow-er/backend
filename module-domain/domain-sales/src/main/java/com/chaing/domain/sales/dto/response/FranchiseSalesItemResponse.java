package com.chaing.domain.sales.dto.response;

import com.chaing.domain.sales.entity.SalesItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FranchiseSalesItemResponse(
        @NotBlank
        String lot
) {
    public static FranchiseSalesItemResponse from(SalesItem salesItem) {
        return FranchiseSalesItemResponse.builder()
                .lot(salesItem.getLot())
                .build();
    }
}
