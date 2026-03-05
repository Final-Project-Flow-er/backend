package com.chaing.api.controller.franchise;


import com.chaing.api.facade.franchise.FranchiseInventoryFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.StockSearchRequest;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.InventoryAlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "FranchiseInventory API", description = "가맹점 재고 관련 API")
@RequestMapping("/api/v1/franchise/inventory")
@RequiredArgsConstructor
public class FranchiseInventoryController {

    private final FranchiseInventoryFacade franchiseInventoryFacade;
    @Operation(summary = "특정 가맹점 재고 조회", description = "특정 가맹점 재고 전체 조회합니다.")
    @GetMapping("/{franchiseId}")
    public ResponseEntity<ApiResponse<List<FranchiseInventoryProductResponse>>> getFranchiseStock(
            @PathVariable Long franchiseId,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status
    ) {

        StockSearchRequest request = new StockSearchRequest(productCode, name, status);

        return ResponseEntity.ok(
                ApiResponse.success(franchiseInventoryFacade.getFranchiseStock(franchiseId, request))
        );
    }

    @Operation(summary = "재고 증가", description = "재고 증가와 동시에 로그 기록합니다.")
    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<Void>> increaseInventory(@Valid @RequestBody InventoryBatchRequest inventoryBatchRequest){
        return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.increaseInventory(inventoryBatchRequest)));
    }
    
    @Operation(summary = "재고 감소", description = "재고 감소와 동시에 로그 기록합니다.")
    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<Void>> decreaseInventory(@Valid @RequestBody InventoryBatchRequest inventoryBatchRequest){
        return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.decreaseInventory(inventoryBatchRequest)));
    }

    @Operation(summary = "재고 알림 조회", description = "안전재고 부족 및 유통기한 관련 알림 목록을 조회합니다."
    )
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<InventoryAlertResponse>> getInventoryAlerts(
            @PathVariable Long franchiseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.getInventoryAlerts(franchiseId)));
    }

    @Operation(summary = "특정 가맹점의 재고 중분류", description = "본사가 특정 가맹점의 제조일자별 수량을 확인합니다.")
    @GetMapping("/{franchiseId}/batches/{productId}")
    public ResponseEntity<ApiResponse<List<FranchiseInventoryBatchResponse>>> getFranchiseBatches(
            @PathVariable Long franchiseId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.getFranchiseBatches(franchiseId, productId)));
    }

    @Operation(summary = "특정 가맹점의 재고 소분류", description = "본사가 특정 가맹점의 상세 제품 정보를 확인합니다.")
    @GetMapping("/franchises/items")
    public ResponseEntity<ApiResponse<List<FranchiseInventoryItemResponse>>> getFranchiseItems(
            @RequestParam Long franchiseId,
            @RequestParam Long productId,
            @RequestParam LocalDate manufactureDate,
            @RequestParam(required = false) String serialCode,
            @RequestParam(required = false) String boxCode,
            @RequestParam(required = false) LocalDate shippedAt,
            @RequestParam(required = false) LocalDate receivedAt
    ) {
        FranchiseInventoryItemsRequest request = new FranchiseInventoryItemsRequest(productId,serialCode,boxCode,manufactureDate,shippedAt,receivedAt);
        return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.getFranchiseItems(franchiseId, request)));
    }
}
