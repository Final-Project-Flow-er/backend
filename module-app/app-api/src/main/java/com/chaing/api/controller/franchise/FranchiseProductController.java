package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.products.request.FranchiseProductSearchRequest;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "FranchiseProduct API", description = "가맹점 상품 관련 API")
@RequestMapping("/api/v1/franchise/product")
public class FranchiseProductController {

    @Operation(summary = "현재 모든 상품 조회", description = "등록되어 있는 상품을 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProducts(@ModelAttribute FranchiseProductSearchRequest franchiseProductSearchRequest){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
