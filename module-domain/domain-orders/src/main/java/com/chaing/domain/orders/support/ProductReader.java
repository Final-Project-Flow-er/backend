package com.chaing.api.dto.franchise.orders.request;

import java.math.BigDecimal;

public interface ProductReader {
    ProductInfo getProduct(Long productId);

    record ProductInfo(
            Long productId,
            BigDecimal unitPrice
    ) {}
}
