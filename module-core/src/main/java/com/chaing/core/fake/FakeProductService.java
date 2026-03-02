package com.chaing.core.fake;

import com.chaing.core.dto.info.ProductInfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeProductService {
    public Map<Long, ProductInfo> getProducts(List<Long> productIds) {
        Map<Long, ProductInfo> response = new HashMap<>();
        response.put(1L,
                ProductInfo.builder()
                        .productId(1L)
                        .productCode("ProductCode")
                        .productName("ProductName")
                        .retailPrice(BigDecimal.valueOf(10000))
                        .costPrice(BigDecimal.valueOf(3000))
                        .tradePrice(BigDecimal.valueOf(5000))
                        .build()
        );

        return response;
    }
}
