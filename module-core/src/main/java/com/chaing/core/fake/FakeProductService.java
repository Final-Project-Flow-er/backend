package com.chaing.core.fake;

import com.chaing.core.dto.info.ProductInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FakeProductService {
    public Map<Long, List<ProductInfo>> getProducts(List<Long> productIds) {
        Map<Long, List<ProductInfo>> response = new HashMap<>();
        response.put(1L, List.of(
                ProductInfo.builder()
                        .productId(1L)
                        .productCode("ProductCode")
                        .productName("ProductName")
                        .build())
        );

        return response;
    }
}
