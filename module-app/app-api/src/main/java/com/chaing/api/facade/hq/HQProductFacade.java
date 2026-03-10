package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.products.request.HQProductCreateRequest;
import com.chaing.api.dto.hq.products.request.HQProductSearchRequest;
import com.chaing.api.dto.hq.products.request.HQProductUpdateRequest;
import com.chaing.api.dto.hq.products.response.HQProductListResponse;
import com.chaing.api.dto.hq.products.response.HQProductResponse;
import com.chaing.domain.products.dto.request.ProductRequest;
import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.request.ProductUpdateRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.exception.ProductErrorCode;
import com.chaing.domain.products.exception.ProductException;
import com.chaing.domain.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class HQProductFacade {
    private final ProductService productService;

    public HQProductListResponse getProducts(HQProductSearchRequest request) {
        ProductSearchRequest productSearchRequest = convertProductSearchRequest(request);
        ProductListResponse productListResponse = productService.getProducts(productSearchRequest);

        List<HQProductResponse> HQProductResponses = productListResponse.products().stream()
                .map(p -> HQProductResponse.builder()
                        .productId(p.product().getProductId())
                        .name(p.product().getName())
                        .productCode(p.product().getProductCode())
                        .description(p.product().getDescription())
                        .size(sizeValid(p.product().getProductCode()))
                        .spicy(spicyValid(p.product().getProductCode()))
                        .kcal(p.product().getKcal())
                        .weight(p.product().getWeight())
                        .safetyStock(p.product().getSafetyStock())
                        .price(p.product().getPrice())
                        .supplyPrice(p.product().getSupplyPrice())
                        .costPrice(p.product().getCostPrice())
                        .startDate(p.product().getSupplyPriceStartDate())
                        .endDate(p.product().getSupplyPriceEndDate())
                        .components(p.component().stream()
                                .map(c -> c.getName())
                                .toList())
                        .status(p.product().getStatus() != null ? p.product().getStatus().name() : null)
                        .build())
                .toList();

        return HQProductListResponse.builder()
                .HQProductList(HQProductResponses)
                .build();

    }

    private String spicyValid(String productCode) {
        if (productCode == null || productCode.length() < 4)
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);
        String spicy = productCode.substring(2, 4);
        return switch (spicy) {
            case "01" -> "순한맛";
            case "02" -> "기본맛";
            case "03" -> "매운맛";
            case "04" -> "아주 매운맛";
            default -> throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);
        };
    }

    private String sizeValid(String productCode) {
        if (productCode == null || productCode.length() < 6)
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);
        String size = productCode.substring(4, 6);
        return switch (size) {
            case "01" -> "1~2인분";
            case "03" -> "3~4인분";
            default -> throw new ProductException(ProductErrorCode.INVALID_PRODUCT_CODE_FORMAT);
        };
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void createProduct(HQProductCreateRequest request) {
        ProductRequest productCreateRequest = convertProductRequest(request);
        productService.createProduct(productCreateRequest);
    }

    // 트랜잭션 따로 붙여야 함
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateProduct(Long productId, HQProductUpdateRequest request) {
        ProductUpdateRequest productUpdateRequest = convertProductUpdateRequest(request);
        productService.updateProduct(productId, productUpdateRequest);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void createProductTypes(String type, String productName) {
        productService.createProductTypes(type, productName);
    }

    private ProductSearchRequest convertProductSearchRequest(HQProductSearchRequest hqProductSearchRequest) {
        return ProductSearchRequest.builder()
                .productCode(hqProductSearchRequest.productCode())
                .name(hqProductSearchRequest.name())
                .status(hqProductSearchRequest.status())
                .sizeCode(hqProductSearchRequest.sizeCode())
                .build();
    }

    private ProductRequest convertProductRequest(HQProductCreateRequest request) {
        return ProductRequest.builder()
                .productCode(request.productCode())
                .name(request.name())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .price(request.price())
                .costPrice(request.costPrice())
                .supplyPrice(request.supplyPrice())
                .safetyStock(request.safetyStock())
                .status(request.status())
                .kcal(request.kcal())
                .weight(request.weight())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .components(request.components())
                .build();
    }

    private ProductUpdateRequest convertProductUpdateRequest(HQProductUpdateRequest request) {
        return new ProductUpdateRequest(
                request.name(),
                request.price(),
                request.originalPrice(),
                request.supplyPrice(),
                request.status(),
                request.baseSafeStock(),
                request.kcal(),
                request.startDate(),
                request.endDate(),
                request.description(),
                request.imageUrl(),
                request.components());
    }

}
