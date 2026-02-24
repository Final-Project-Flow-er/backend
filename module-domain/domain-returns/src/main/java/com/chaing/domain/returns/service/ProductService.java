package com.chaing.domain.returns.service;

import com.chaing.core.dto.returns.request.ReturnToProductRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    public List<ReturnToProductRequest> getProducts(List<Long> productIds) {
        return List.of(
                new ReturnToProductRequest(
                        1L,
                        "ProductName",
                        "ProductCode",
                        BigDecimal.valueOf(5000)
                )
        );
    }
}
