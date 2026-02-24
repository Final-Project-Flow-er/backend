package com.chaing.api.dto.franchise.inventories.request;

public record FranchiseStockSearchRequest(
        Long productId,
        String name,
        String status
) {

}
