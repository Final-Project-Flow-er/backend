package com.chaing.api.dto.hq.inventories.request;

public record HQStockSearchRequest(
        String productCode,
        String name,
        String status
) {

}
