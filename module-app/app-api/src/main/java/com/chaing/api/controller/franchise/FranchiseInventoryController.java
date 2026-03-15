package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.sales.request.SaleScanItemRequest;
import com.chaing.api.dto.franchise.sales.response.ScannedForSaleResponse;
import com.chaing.api.dto.franchise.sales.response.ScannedItemForSaleResponse;
import com.chaing.api.facade.franchise.FranchiseInventoryFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
import com.chaing.domain.inventories.dto.request.StockSearchRequest;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.InventoryAlertResponse;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "FranchiseInventory API", description = "가맹점 재고 관련 API")
@RequestMapping("/api/v1/franchise/inventory")
@RequiredArgsConstructor
public class FranchiseInventoryController {

        private final FranchiseInventoryFacade franchiseInventoryFacade;

        @Operation(summary = "가맹점 전체 재고 조회", description = "가맹점 재고 전체를 조회합니다.")
        @GetMapping
        public ResponseEntity<ApiResponse<List<FranchiseInventoryProductResponse>>> getFranchiseStock(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @RequestParam(required = false) String productCode,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String status) {
                StockSearchRequest request = new StockSearchRequest(productCode, name, status);
                return ResponseEntity.ok(ApiResponse
                                .success(franchiseInventoryFacade.getFranchiseStock(principal.getBusinessUnitId(),
                                                request)));
        }

        @Operation(summary = "재고 증가", description = "재고 증가와 동시에 로그 기록합니다.")
        @PostMapping("/increase")
        public ResponseEntity<ApiResponse<Void>> increaseInventory(
                        @Valid @RequestBody InventoryBatchRequest inventoryBatchRequest) {
                return ResponseEntity
                                .ok(ApiResponse.success(
                                                franchiseInventoryFacade.increaseInventory(inventoryBatchRequest)));
        }

        @Operation(summary = "재고 감소", description = "재고 감소와 동시에 로그 기록합니다.")
        @PostMapping("/decrease")
        public ResponseEntity<ApiResponse<Void>> decreaseInventory(
                        @Valid @RequestBody InventoryBatchRequest inventoryBatchRequest) {
                return ResponseEntity
                                .ok(ApiResponse.success(
                                                franchiseInventoryFacade.decreaseInventory(inventoryBatchRequest)));
        }

        @Operation(summary = "가맹점 유통기한 및 안전재고 알림", description = "안전재고 부족 및 유통기한 관련 알림 목록을 조회합니다.")
        @GetMapping("/alerts")
        public ResponseEntity<ApiResponse<InventoryAlertResponse>> getInventoryAlerts(
                        @AuthenticationPrincipal UserPrincipal principal) {
                return ResponseEntity.ok(ApiResponse.success(
                                franchiseInventoryFacade.getInventoryAlerts(principal.getBusinessUnitId())));
        }

        @Operation(summary = "상품별 상세 재고(배치) 조회", description = "특정 상품의 제조일자별 수량을 확인합니다.")
        @GetMapping("/batches/{productId}")
        public ResponseEntity<ApiResponse<Page<FranchiseInventoryBatchResponse>>> getFranchiseBatches(
                @AuthenticationPrincipal UserPrincipal principal,
                @PathVariable Long productId,
                @PageableDefault(size = 20) Pageable pageable) {
            return ResponseEntity.ok(ApiResponse.success(
                    franchiseInventoryFacade.getFranchiseBatches(principal.getBusinessUnitId(), productId, pageable)));
        }

        @Operation(summary = "상세 재고 소분류 리스트 조회", description = "상세 제품 정보를 확인합니다.")
        @PostMapping("/items")
        public ResponseEntity<ApiResponse<Page<FranchiseInventoryItemResponse>>> getFranchiseItems(
                @AuthenticationPrincipal UserPrincipal principal,
                @Valid @RequestBody FranchiseInventoryItemsRequest request,
                @PageableDefault(size = 20) Pageable pageable) {
            return ResponseEntity.ok(ApiResponse.success(
                    franchiseInventoryFacade.getFranchiseItems(principal.getBusinessUnitId(), request, pageable)));
        }

        @Operation(summary = "폐기 처리", description = "제품을 폐기합니다.")
        @PostMapping("/disposal")
        public ResponseEntity<ApiResponse<Void>> disposalInventory(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @Valid @RequestBody DisposalRequest request) {
                return ResponseEntity.ok(ApiResponse
                                .success(franchiseInventoryFacade.disposalInventory(request,
                                                principal.getBusinessUnitId())));
        }

        @Operation(summary = "안전재고 설정", description = "관리자가 직접 안전재고를 설정합니다.")
        @PostMapping("/set/safety-stock")
        public ResponseEntity<ApiResponse<Void>> setSafetyStock(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @RequestBody SafetyStockRequest request) {
                SafetyStockRequest validRequest = new SafetyStockRequest(
                                "FRANCHISE", principal.getBusinessUnitId(), request.productId(), request.safetyStock());
                franchiseInventoryFacade.setSafetyStock(validRequest);
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "안전재고 초기화", description = "시스템이 계산한 권장 안전재고로 돌아갑니다.")
        @PostMapping("/reset/safety-stock/{productId}")
        public ResponseEntity<ApiResponse<Void>> resetSafetyStock(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @PathVariable Long productId) {
                franchiseInventoryFacade.resetSafetyStock(principal.getBusinessUnitId(), productId);
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "안전재고 설정 비밀번호 확인", description = "관리자 비밀번호가 실제 사용자 비밀번호와 일치하는지 확인합니다.")
        @PostMapping("/verify-password")
        public ResponseEntity<ApiResponse<Boolean>> verifyPassword(
                        @AuthenticationPrincipal UserPrincipal principal,
                        @RequestBody Map<String, String> request) {
                return ResponseEntity.ok(ApiResponse.success(
                                franchiseInventoryFacade.verifyAdminPassword(principal.getId(),
                                                request.get("password"))));
        }

        @Operation(summary = "판매 제품 목록 조회", description = "스캔된 판매 목록이 화면에 출력된다.")
        @GetMapping("/scanned-sales-list")
        public ResponseEntity<ApiResponse<ScannedForSaleResponse>> getScannedSalesList(
                @RequestParam List<@NotBlank String> serialCodes,
                @AuthenticationPrincipal UserPrincipal Principal
        ) {
            return ResponseEntity.ok(ApiResponse.success(franchiseInventoryFacade.getFranchiseItemsForSale(serialCodes, Principal.getBusinessUnitId())));
        }
}
