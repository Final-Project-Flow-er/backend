package com.chaing.domain.orders.support;

import java.math.BigDecimal;

public interface ProductReader {
    ProductInfo getProduct(String productCode);

    record ProductInfo(
            Long productId,
            String productCode,
            BigDecimal unitPrice
    ) {}
}
