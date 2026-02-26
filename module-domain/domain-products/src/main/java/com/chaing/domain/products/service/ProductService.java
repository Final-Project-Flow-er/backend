package com.chaing.domain.products.service;

import com.chaing.domain.products.dto.request.ProductRequest;
import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.request.ProductUpdateRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.entity.ProductComponent;
import com.chaing.domain.products.entity.ProductType;
import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.exception.ProductErrorCode;
import com.chaing.domain.products.exception.ProductException;
import com.chaing.domain.products.repository.ProductComponentRepository;
import com.chaing.domain.products.repository.ProductRepository;
import com.chaing.domain.products.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductComponentRepository productComponentRepository;

    public ProductListResponse getProducts(ProductSearchRequest productSearchRequest) {
        return productRepository.getProducts(productSearchRequest);
    }

    @Transactional
    public void createProduct(ProductRequest request) {

        // 1. 상태 파싱
        ProductStatus status = parseStatus(request.status());

        // 2. 상품코드 → 타입 해석
        Long productTypeId = existType(request.productCode());

        // 3. 상품 생성
        Product product = Product.builder()
                .productCode(request.productCode())
                .name(request.name())
                .description(request.description())
                .productTypeId(productTypeId)
                .imageUrl(request.imageUrl())
                .price(request.price())
                .costPrice(request.costPrice())
                .supplyPrice(request.supplyPrice())
                .safetyStock(request.safetyStock())
                .status(status)
                .kcal(request.kcal())
                .weight(request.weight())
                .supplyPriceStartDate(request.startDate())
                .supplyPriceEndDate(request.endDate())
                .build();

        productRepository.save(product);

        // 4. 구성품 매핑 생성
        if (request.componentIds() != null && !request.componentIds().isEmpty()) {

            List<ProductComponent> mappings =
                    request.componentIds().stream()
                            .map(componentId -> ProductComponent.builder()
                                    .productId(product.getProductId())
                                    .componentId(componentId)
                                    .build())
                            .toList();

            productComponentRepository.saveAll(mappings);
        }
    }

    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest req) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 1. 상품 정보 수정
        product.update(req);

        // 2. 구성품 동기화
        if (req.componentIds() != null) {
            syncComponents(productId, req.componentIds());
        }
    }
    @Transactional
    public void createProductTypes(String type, String productName) {
        if (productTypeRepository.existsByProductType(type)) {
            throw new ProductException(ProductErrorCode.DUPLICATE_PRODUCT_CODE);
        }
        ProductType pt = ProductType.builder()
                .productType(type)
                .name(productName)
                .build();
        productTypeRepository.save(pt);
    }

    public Long existType(String productCode) {

        if (productCode == null || productCode.length() < 2)
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);

        String productType = productCode.substring(0, 2).toUpperCase();

        return productTypeRepository.findByProductType(productType)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_TYPE_NOT_FOUND))
                .getId();
    }

    private void syncComponents(Long productId, List<Long> requestIds) {

        List<ProductComponent> current = productComponentRepository.findByProductId(productId);

        Set<Long> currentIds = current.stream()
                .map(ProductComponent::getComponentId)
                .collect(Collectors.toSet());

        Set<Long> requestSet = new HashSet<>(requestIds);

        // 삭제 대상
        List<ProductComponent> toDelete = current.stream()
                .filter(pc -> !requestSet.contains(pc.getComponentId()))
                .toList();

        productComponentRepository.deleteAll(toDelete);

        // 추가 대상
        List<ProductComponent> toAdd = requestSet.stream()
                .filter(id -> !currentIds.contains(id))
                .map(id -> ProductComponent.builder()
                        .productId(productId)
                        .componentId(id)
                        .build())
                .toList();

        productComponentRepository.saveAll(toAdd);
    }

    private ProductStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }

        try {
            return ProductStatus.valueOf(
                    rawStatus.trim().toUpperCase(java.util.Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }
    }
}
