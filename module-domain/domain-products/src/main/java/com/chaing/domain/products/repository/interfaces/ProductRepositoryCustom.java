package com.chaing.domain.products.repository.interfaces;

import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.response.ProductInfoResponse;
import com.chaing.domain.products.dto.response.ProductListResponse;

import java.util.List;

public interface ProductRepositoryCustom {
    ProductListResponse getProducts(ProductSearchRequest productSearchRequest);
    List<ProductInfoResponse> getInventoryProducts(String productCode, String name);
}
