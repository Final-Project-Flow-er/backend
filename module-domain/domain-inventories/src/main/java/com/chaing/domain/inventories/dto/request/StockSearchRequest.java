package com.chaing.domain.inventories.dto.request;

public record StockSearchRequest(
        String productCode,
        String name,
        String status
) {

}
