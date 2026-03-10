package com.chaing.domain.products.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.products.dto.request.ProductRequest;
import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.request.ProductUpdateRequest;
import com.chaing.domain.products.dto.response.ProductInfoResponse;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.entity.Component;
import com.chaing.domain.products.entity.Product;
import com.chaing.domain.products.entity.ProductComponent;
import com.chaing.domain.products.entity.ProductType;
import com.chaing.domain.products.enums.ProductStatus;
import com.chaing.domain.products.exception.ProductErrorCode;
import com.chaing.domain.products.exception.ProductException;
import com.chaing.domain.products.repository.ComponentRepository;
import com.chaing.domain.products.repository.ProductComponentRepository;
import com.chaing.domain.products.repository.ProductRepository;
import com.chaing.domain.products.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductComponentRepository productComponentRepository;
    private final ComponentRepository componentRepository;

    public ProductListResponse getProducts(ProductSearchRequest productSearchRequest) {
        return productRepository.getProducts(productSearchRequest);
    }

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
        if (request.components() != null && !request.components().isEmpty()) {
            List<Long> componentIds = resolveComponentIds(request.components());
            List<ProductComponent> mappings = componentIds.stream()
                    .map(componentId -> ProductComponent.builder()
                            .productId(product.getProductId())
                            .componentId(componentId)
                            .build())
                    .toList();

            productComponentRepository.saveAll(mappings);
        }
    }

    public void updateProduct(Long productId, ProductUpdateRequest req) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 1. 상품 정보 수정
        product.update(req);

        // 2. 구성품 동기화
        if (req.components() != null) {
            List<Long> componentIds = resolveComponentIds(req.components());
            syncComponents(productId, componentIds);
        }
    }

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

    private List<Long> resolveComponentIds(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        return names.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> getOrCreateComponent(name).getComponentId())
                .distinct()
                .toList();
    }

    private Component getOrCreateComponent(String name) {
        return componentRepository.findByName(name)
                .orElseGet(() -> {
                    try {
                        return componentRepository.save(
                                Component.builder()
                                        .name(name)
                                        .build());
                    } catch (DataIntegrityViolationException e) {
                        return componentRepository.findByName(name)
                                .orElseThrow(() -> e);
                    }
                });
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
                    rawStatus.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    public Map<Long, ProductInfo> getProductInfos(List<Long> productIds) {
        List<Product> products = productRepository.findAllByProductIdIn(productIds);

        if (products == null || products.isEmpty()) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        return products.stream()
                .collect(Collectors.toMap(
                        Product::getProductId,
                        entry -> ProductInfo.builder()
                                .productId(entry.getProductId())
                                .productCode(entry.getProductCode())
                                .productName(entry.getName())
                                .retailPrice(entry.getPrice())
                                .costPrice(entry.getCostPrice())
                                .tradePrice(entry.getSupplyPrice())
                                .build()));
    }

    // 제품 정보 전체 반환
    // return: Map<productId, ProductInfo>
    public Map<Long, ProductInfo> getAllProductInfo() {
        List<Product> products = productRepository.findAllByStatus(ProductStatus.ON_SALE);

        if (products == null || products.isEmpty()) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        return products.stream()
                .collect(Collectors.toMap(
                        Product::getProductId,
                        product -> ProductInfo.builder()
                                .productId(product.getProductId())
                                .productCode(product.getProductCode())
                                .productName(product.getName())
                                .retailPrice(product.getPrice())
                                .costPrice(product.getCostPrice())
                                .tradePrice(product.getSupplyPrice())
                                .build()));
    }

    public Map<Long, Integer> getWeightsByProductIds(List<Long> productIds) {

        List<Product> products = productRepository.findAllByProductIdIn(productIds);

        Map<Long, Integer> weightMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Product::getWeight));

        if (weightMap.size() != productIds.stream().distinct().count()) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        return weightMap;
    }

    // 모든 상품 Id 조회
    public List<Long> getAllProductIds() {
        return productRepository.findAllProductIds();
    }

    public List<ProductInfoResponse> getInventoryProducts(String productCode, String name) {
        return productRepository.getInventoryProducts(productCode, name);
    }
}
