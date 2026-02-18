package com.chaing.domain.orders.infrastructure;

import com.chaing.domain.orders.support.ProductReader;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FakeProductReader implements ProductReader {

    @Override
    public ProductInfo getProduct(String productCode) {
        return new ProductInfo(
                1L,
                "ProductCode",
                BigDecimal.valueOf(10000)
        );
    }
}
