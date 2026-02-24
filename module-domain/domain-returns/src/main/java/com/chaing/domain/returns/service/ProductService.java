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
                        BigDecimal.valueOf(5000)));
    }

    public List<FranchiseReturnProductInfo> getProduct(String returnCode) {
        return List.of(
                FranchiseReturnProductInfo.builder()
                        .boxCode("BoxCode")
                        .serialCode("OR0101")
                        .productCode("ProductCode")
                        .productName("ProductName")
                        .unitPrice(BigDecimal.valueOf(5000))
                        .build());
    }
}
