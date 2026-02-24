package com.chaing.api.controller.hq;


import com.chaing.api.dto.hq.inventories.request.HQFranchiseItemsRequest;
import com.chaing.api.dto.hq.inventories.request.HQInventoryBatchRequest;
import com.chaing.api.dto.hq.inventories.request.HQStockSearchRequest;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "HqInventory API", description = "본사 재고 관련 관련 API")
@RequestMapping("/api/v1/hq/inventory")
public class HQInventoryController {

    @Operation(summary = "현재 재고 조회", description = "현재 재고 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getStock(@ModelAttribute HQStockSearchRequest hqStockSearchRequest) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "재고 증가", description = "재고 증가와 동시에 로그 기록한다.")
    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<?>> increaseInventory(@RequestBody HQInventoryBatchRequest hqInventoryBatchRequest){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "재고 감소", description = "재고 감소와 동시에 로그 기록한다.")
    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<?>> decreaseInventory(@RequestBody HQInventoryBatchRequest hqInventoryBatchRequest){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "안전 재고 부족 알림", description = "안전 재고 수량보다 현재 재고가 적은 제품 목록을 가져옵니다. (알림)")
    @GetMapping("/safety-stock")
    public ResponseEntity<ApiResponse<?>> getSafetyStock() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "원하는 상품의 중분류", description = "원하는 상품의 제조일자와 해당 수량목록을 가져온다.")
    @GetMapping("/batches/{productId}")
    public ResponseEntity<ApiResponse<?>> getBatches(@PathVariable("productId") Long productId){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "원하는 상품의 소분류", description = "제품의 자세한 정보를 가져온다.")
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<?>> getItems(
            @RequestParam Long productId,
            @RequestParam LocalDate productDate
            ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "특정 가맹점 재고 조회", description = "특정 가맹점 재고 전체 조회한다.")
    @GetMapping("/franchises/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> getFranchiseStock(@PathVariable Long franchiseId){
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "특정 가맹점의 재고 중분류", description = "본사가 특정 가맹점의 제조일자별 수량을 확인한다.")
    @GetMapping("/franchises/{franchiseId}/batches/{productId}")
    public ResponseEntity<ApiResponse<?>> getStoreBatches(
            @PathVariable Long franchiseId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "특정 가맹점의 재고 소분류", description = "본사가 특정 가맹점의 상세 제품 정보를 확인한다.")
    @PostMapping("/franchises/items/search")
    public ResponseEntity<ApiResponse<?>> getFranchiseItems(@RequestBody HQFranchiseItemsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}
