package com.chaing.domain.inventories.dto.request;

import com.chaing.core.enums.LogType;

import java.util.List;

public record InventoryBoxRequest(
        String boxCode,
        LogType logType,        // 배송 중, 반품 입고, 반품 출고, 입고, 출고
        String productCode,     // 제품 코드 (예: OR0101)
        String productName,
        List<InventoryRequest> productList
) {
}
