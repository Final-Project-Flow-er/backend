package com.chaing.api.controller.hq;

import com.chaing.api.facade.hq.HQInventoryFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
import com.chaing.domain.inventories.dto.request.StockSearchRequest;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryProductResponse;
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

@RequiredArgsConstructor
@RestController
@Tag(name = "HQInventory API", description = "본사 재고 관련 API")
@RequestMapping("/api/v1/hq/inventory")
public class HQInventoryController {
    private final HQInventoryFacade hqInventoryFacade;

    @Operation(summary = "현재 재고 조회", description = "현재 재고 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HQInventoryProductResponse>>> getStock(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        StockSearchRequest request = new StockSearchRequest(productCode, name, status);
        return ResponseEntity.ok(
                ApiResponse.success(hqInventoryFacade.getStock(request)));
    }

    @Operation(summary = "재고 증가", description = "재고 증가와 동시에 로그 기록합니다.")
    @PostMapping("/increase")
    public ResponseEntity<ApiResponse<Void>> increaseInventory(
            @Valid @RequestBody InventoryBatchRequest inventoryBatchRequest) {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.increaseInventory(inventoryBatchRequest)));
    }

    @Operation(summary = "재고 감소", description = "재고 감소와 동시에 로그 기록합니다.")
    @PostMapping("/decrease")
    public ResponseEntity<ApiResponse<Void>> decreaseInventory(
            @Valid @RequestBody InventoryBatchRequest inventoryBatchRequest) {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.decreaseInventory(inventoryBatchRequest)));
    }

    @Operation(summary = "재고 알림 조회", description = "안전재고 부족 및 유통기한 관련 알림 목록을 조회합니다.")
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<InventoryAlertResponse>> getInventoryAlerts() {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getInventoryAlerts()));
    }

    @Operation(summary = "원하는 상품의 중분류", description = "원하는 상품의 제조일자와 해당 수량목록을 가져옵니다.")
    @GetMapping("/batches/{productId}")
    public ResponseEntity<ApiResponse<List<HQInventoryBatchResponse>>> getBatches(
            @PathVariable("productId") Long productId) {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getBatches(productId)));
    }

    @Operation(summary = "원하는 상품의 소분류", description = "제품의 자세한 정보를 가져옵니다.")
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<HQInventoryItemResponse>>> getItems(
            @RequestParam Long productId,
            @RequestParam(required = false) String serialCode,
            @RequestParam LocalDate manufactureDate,
            @RequestParam(required = false) LocalDate shippedAt,
            @RequestParam(required = false) LocalDate receivedAt) {
        HQInventoryItemsRequest request = new HQInventoryItemsRequest(productId, serialCode, manufactureDate, shippedAt,
                receivedAt);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getItems(request)));
    }

    @Operation(summary = "특정 가맹점 재고 조회", description = "특정 가맹점 재고 전체 조회합니다.")
    @GetMapping("/franchises/{franchiseId}")
    public ResponseEntity<ApiResponse<List<FranchiseInventoryProductResponse>>> getFranchiseStock(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @PathVariable Long franchiseId) {
        StockSearchRequest request = new StockSearchRequest(productCode, name, status);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getFranchiseStock(franchiseId, request)));
    }

    @Operation(summary = "특정 가맹점의 재고 중분류", description = "본사가 특정 가맹점의 제조일자별 수량을 확인합니다.")
    @GetMapping("/franchises/{franchiseId}/batches/{productId}")
    public ResponseEntity<ApiResponse<List<FranchiseInventoryBatchResponse>>> getFranchiseBatches(
            @PathVariable Long franchiseId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getFranchiseBatches(franchiseId, productId)));
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
            @RequestParam(required = false) LocalDate receivedAt) {
        FranchiseInventoryItemsRequest request = new FranchiseInventoryItemsRequest(productId, serialCode, boxCode,
                manufactureDate, shippedAt, receivedAt);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.getFranchiseItems(franchiseId, request)));
    }

    @Operation(summary = "안전 재고 계산 및 유통기한 상세 갱신", description = "모든 상품 안전재고와 안전재고를 자동으로 재계산하여 갱신합니다.")
    @PostMapping("/safety-stock/refresh")
    public ResponseEntity<ApiResponse<Void>> calculateSafetyStock() {
        hqInventoryFacade.calculateSafetyStock();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "폐기 처리", description = "제품을 폐기합니다.")
    @PostMapping("/disposal")
    public ResponseEntity<ApiResponse<Void>> disposalInventory(@Valid @RequestBody DisposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(hqInventoryFacade.disposalInventory(request)));
    }

    @Operation(summary = "안전재고 설정", description = "관리자가 직접 안전재고를 설정합니다.")
    @PostMapping("/set/safety-stock")
    public ResponseEntity<ApiResponse<Void>> setSafetyStock(
            @RequestBody SafetyStockRequest request) {
        hqInventoryFacade.setSafetyStock(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
