package com.chaing.api.controller.hq;


import com.chaing.api.dto.hq.products.request.HQProductSearchRequest;
import com.chaing.api.dto.hq.products.request.HQProductTypeCreateRequest;
import com.chaing.api.dto.hq.products.request.HQProductUpdateRequest;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HqProduct API", description = "본사 상품 관련 API")
@RequestMapping("/api/v1/hq/product")
public class HQProductController {

    @Operation(summary = "현재 모든 상품 조회", description = "등록되어 있는 상품을 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProducts(@ModelAttribute HQProductSearchRequest hqProductSearchRequest){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 추가", description = "입력한 정보로 상품을 추가한다.")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상품 수정", description = "입력한 정보로 상품을 수정한다.")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable Long productId,
            @RequestBody HQProductUpdateRequest hqProductUpdateRequest) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "제품타입 추가", description = "제품 타입을 추가한다.")
    @PostMapping("/product-types")
    public ResponseEntity<ApiResponse<?>> createProductTypes(
            @RequestBody HQProductTypeCreateRequest hqProductTypeCreateRequest
            ){
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
