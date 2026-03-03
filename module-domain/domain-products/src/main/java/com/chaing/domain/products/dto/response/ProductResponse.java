package com.chaing.domain.products.dto.response;

import com.chaing.domain.products.entity.Component;
import com.chaing.domain.products.entity.Product;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponse(
        Product product,
        List<Component> component
) {
    @QueryProjection
    public ProductResponse(Product product, List<Component> component) {
        this.product = product;
        this.component = component;
    }
}