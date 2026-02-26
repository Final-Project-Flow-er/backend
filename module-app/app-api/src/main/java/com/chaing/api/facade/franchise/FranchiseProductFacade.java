package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.products.request.FranchiseProductSearchRequest;
import com.chaing.api.dto.franchise.products.response.FranchiseProductListResponse;
import com.chaing.api.dto.franchise.products.response.FranchiseProductResponse;
import com.chaing.domain.products.dto.request.ProductSearchRequest;
import com.chaing.domain.products.dto.response.ProductListResponse;
import com.chaing.domain.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class FranchiseProductFacade {
    private final ProductService productService;

    public FranchiseProductListResponse getProducts(FranchiseProductSearchRequest request) {
        ProductSearchRequest productSearchRequest = convertProductSearchRequest(request);
        ProductListResponse productListResponse = productService.getProducts(productSearchRequest);


        List<FranchiseProductResponse> franchiseProductResponses =
                productListResponse.products().stream()
                .map( p -> FranchiseProductResponse.builder()
                        .name(p.product().getName())
                        .productCode(p.product().getProductCode())
                        .description(p.product().getDescription())
                        .size(sizeValid(p.product().getProductCode()))
                        .kcal(p.product().getKcal())
                        .spicy(spicyValid(p.product().getProductCode()))
                        .weight(p.product().getWeight())
                        .safetyStock(p.product().getSafetyStock())
                        .price(p.product().getPrice())
                        .supplyPrice(p.product().getSupplyPrice())
                        .components(p.component().stream()
                                .map(c -> c.getName())
                                .toList())
                        .build())
                        .toList();

        return FranchiseProductListResponse.builder()
                .franchiseProductList(franchiseProductResponses)
                .build();
    }

    private ProductSearchRequest convertProductSearchRequest(FranchiseProductSearchRequest request) {
        return ProductSearchRequest.builder()
                .productCode(request.productCode())
                .name(request.name())
                .status(request.status())
                .sizeCode(request.sizeCode())
                .build();
    }

    private String spicyValid(String productCode){
        String spicy = productCode.substring(2,4);
        return switch (spicy){
            case "01"  -> "순한맛";
            case "02" -> "기본맛";
            case "03" -> "매운맛";
            case "04" -> "아주 매운맛";
            default -> null;
        };
    }


    private String sizeValid(String productCode){
        String size = productCode.substring(4,6);
        return switch (size) {
            case "01" -> "1~2인분";
            case "03" -> "3~4인분";
            default -> null;
        };
    }
}
