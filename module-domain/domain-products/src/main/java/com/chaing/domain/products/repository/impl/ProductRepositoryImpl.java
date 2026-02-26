package com.chaing.domain.products.repository.impl;


import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.dto.response.ProductResponse;
import com.chaing.domain.products.entity.Component;
import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.entity.QComponent;
import com.chaing.domain.products.entity.QProduct;
import com.chaing.domain.products.entity.QProductComponent;
import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.exception.ProductErrorCode;
import com.chaing.domain.products.exception.ProductException;
import com.chaing.domain.products.repository.interfaces.ProductRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ProductListResponse getProducts(ProductSearchRequest req) {

        QProduct product = QProduct.product;
        QComponent component = QComponent.component;
        QProductComponent pc = QProductComponent.productComponent;

        // 1. Product만 fetch
        List<Product> productList = queryFactory
                .selectFrom(product)
                .where(
                        containsProductCode(req.productCode()),
                        containsName(req.name()),
                        containsStatus(req.status()),
                        containsSizeCode(req.sizeCode())
                )
                .fetch();

        // 2. Component fetch + DTO 변환
        List<ProductResponse> products = productList.stream()
                .map(p -> {
                    List<Component> components = queryFactory
                            .select(component)
                            .from(pc)
                            .leftJoin(component).on(component.componentId.eq(pc.componentId))
                            .where(pc.productId.eq(p.getProductId()))
                            .fetch();

                    return new ProductResponse(p, components);
                })
                .toList();

        // 3. Response 반환
        return ProductListResponse.builder()
                .products(products)
                .build();
    }

    // productCode 존재 여부 확인
    private BooleanExpression containsProductCode(String productCode){
        return productCode != null ? QProduct.product.productCode.contains(productCode) : null;
    }

    private BooleanExpression containsName(String name){
        return name != null ? QProduct.product.name.contains(name) : null;
    }

    private BooleanExpression containsStatus(String status){
        if (status == null || status.isBlank()) return null;

        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            return QProduct.product.status.eq(productStatus);
        } catch (IllegalArgumentException e){
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);
        }
    }

    private BooleanExpression containsSizeCode(String sizeCode){
        return sizeCode != null ? QProduct.product.productCode.endsWith(sizeCode) : null;
    }


}
