package com.chaing.domain.products.repository.interfaces;

import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;

public interface ProductRepositoryCustom {
    ProductListResponse getProducts(ProductSearchRequest productSearchRequest);
}
