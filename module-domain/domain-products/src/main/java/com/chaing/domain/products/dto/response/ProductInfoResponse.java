package com.chaing.domain.products.dto.response;

public record ProductInfoResponse(
        Long productId,
        String productCode,
        String name
) {}