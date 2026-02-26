package com.chaing.api.controller.hq;


import com.chaing.api.dto.hq.products.request.HQProductSearchRequest;
import com.chaing.api.dto.hq.products.request.HQProductUpdateRequest;
import com.chaing.api.dto.hq.products.request.HQProductCreateRequest;
import com.chaing.api.dto.hq.products.response.HQProductListResponse;
import com.chaing.api.facade.hq.HQProductFacade;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "HqProduct API", description = "본사 상품 관련 API")
@RequestMapping("/api/v1/hq/product")
public class HQProductController {

    private final HQProductFacade hqProductFacade;

    @Operation(summary = "현재 모든 상품 조회", description = "등록되어 있는 상품을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<HQProductListResponse>> getProducts(
            @ModelAttribute HQProductSearchRequest hqProductSearchRequest)
    {
        return ResponseEntity.ok(ApiResponse.success(hqProductFacade.getProducts(hqProductSearchRequest)));
    }

    @Operation(summary = "상품 추가", description = "입력한 정보로 상품을 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<Void> createProduct(
            @RequestBody HQProductCreateRequest request
    ) {
        hqProductFacade.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "상품 수정", description = "입력한 정보로 상품을 수정합니다.")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable Long productId,
            @RequestBody HQProductUpdateRequest request) {
        {
            hqProductFacade.updateProduct(productId,request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

    @Operation(summary = "제품타입 추가", description = "제품 타입을 추가합니다.")
    @PostMapping("/product-types")
    public ResponseEntity<ApiResponse<Void>> createProductTypes(
            @RequestParam String type,
            @RequestParam String productName
    ) {
        hqProductFacade.createProductTypes(type, productName);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
