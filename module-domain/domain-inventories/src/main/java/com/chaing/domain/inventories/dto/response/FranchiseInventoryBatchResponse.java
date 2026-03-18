package com.chaing.domain.inventories.dto.response;

import java.time.LocalDate;

public record FranchiseInventoryBatchResponse(

        LocalDate manufactureDate,      // 제조일
        Integer totalQuantity,          // 총 수량
        Integer availableQuantity       // 가용 수량

) {}
