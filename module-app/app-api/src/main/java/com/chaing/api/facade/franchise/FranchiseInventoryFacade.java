package com.chaing.api.facade.franchise;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryBoxRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.dto.request.StockSearchRequest;
import com.chaing.domain.inventories.dto.response.ExpirationAlertResponse;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.InventoryAlertResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockAlertResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.servie.InventoryService;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import com.chaing.domain.products.dto.response.ProductInfoResponse;
import com.chaing.domain.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FranchiseInventoryFacade {
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final InventoryLogService inventoryLogService;

    // 특정 가맹점 대분류
    public List<FranchiseInventoryProductResponse> getFranchiseStock(Long franchiseId, StockSearchRequest request) {
        List<ProductInfoResponse> products = productService.getInventoryProducts(request.productCode(), request.name());
        List<Long> ids = products.stream()
                .map(ProductInfoResponse::productId)
                .toList();
        Map<Long, InventoryProductInfoResponse> productInfos = inventoryService.getFranchiseStock(franchiseId, ids,
                request.status());

        return products.stream()
                .map(p -> {

                    InventoryProductInfoResponse info = productInfos.get(p.productId());

                    if (info == null) {
                        return new FranchiseInventoryProductResponse(
                                p.productId(),
                                p.productCode(),
                                p.name(),
                                0,
                                p.productCode().substring(4, 6),
                                0,
                                null);
                    }

                    return new FranchiseInventoryProductResponse(
                            p.productId(),
                            p.productCode(),
                            p.name(),
                            info.totalQuantity(),
                            p.productCode().substring(4, 6),
                            info.safetyStock(),
                            info.status());
                })
                .toList();
    }

    // 특정 가맹점 중분류
    public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
        return inventoryService.getFranchiseBatches(franchiseId, productId);
    }

    // 특정 가맹점 소분류
    public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
            FranchiseInventoryItemsRequest request) {
        return inventoryService.getFranchiseItems(franchiseId, request);
    }

    // 입고 처리
    @Transactional
    public Void increaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        // 재고 증가 로그 기록
        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        // 재고 증가
        LocationType toType = LocationType.valueOf(inventoryBatchRequest.toLocationType().toUpperCase());
        inventoryService.franchiseIncreaseInventory(inventoryBatchRequest);

        // 해당 재고 삭제
        LocationType fromType = LocationType.valueOf(inventoryBatchRequest.fromLocationType().toUpperCase());

        if (fromType == LocationType.FRANCHISE) {
            inventoryService.deleteFranchiseInventory(inventoryBatchRequest.fromLocationId(),
                    inventoryBatchRequest.boxes());
        } else if (fromType == LocationType.FACTORY) {
            inventoryService.deleteFactoryInventory(inventoryBatchRequest.boxes());
        } else {
            inventoryService.deleteHqInventory(inventoryBatchRequest.boxes());
        }

        return null;
    }

    // 출고 처리
    @Transactional
    public Void decreaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        // 해당 제품들 다 배송 중 상태로 변경
        inventoryService.updateFranchiseShippingStatus(inventoryBatchRequest.fromLocationId(),
                inventoryBatchRequest.boxes());
        // 로그 기록 (출고 로그 기록)
        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);
        return null;
    }

    // 안전재고 유통기한 알림
    @Transactional(readOnly = true)
    public InventoryAlertResponse getInventoryAlerts(Long franchiseId) {
        // 안전재고 검색
        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("FRANCHISE", franchiseId);

        // 유통기한 검색
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FRANCHISE",
                franchiseId);

        // 안전재고, 유통기한 이름, 코드 매칭을 위해 ID불러옴
        List<Long> ids = productService.getAllProductIds();

        Map<Long, ProductInfo> products = productService.getProductInfos(ids);

        // 코드와 이름 조합
        List<SafetyStockAlertResponse> safetyStockAlerts = safetyStockAlert.stream()
                .map(k -> new SafetyStockAlertResponse(
                        products.get(k.productId()).productCode(),
                        products.get(k.productId()).productName(),
                        k.currentQuantity(),
                        k.safetyStock()))
                .toList();

        // 코드와 이름 조합
        List<ExpirationAlertResponse> expirationAlert = expirationAlerts.stream()
                .map(k -> new ExpirationAlertResponse(
                        products.get(k.productId()).productName(),
                        k.manufactureDate(),
                        k.quantity(),
                        k.daysUntilExpiration()))
                .toList();

        return InventoryAlertResponse.builder()
                .safetyStockAlerts(safetyStockAlerts)
                .expirationAlerts(expirationAlert)
                .build();
    }

    public List<InventoryLogCreateRequest> convert(InventoryBatchRequest request) {
        LocationType fromType = LocationType.valueOf(request.fromLocationType().toUpperCase());
        LocationType toType = LocationType.valueOf(request.toLocationType().toUpperCase());
        ActorType actorType = ActorType.valueOf(request.fromLocationType().toUpperCase());

        List<InventoryLogCreateRequest> result = new ArrayList<>();

        for (InventoryBoxRequest box : request.boxes()) {

            int quantity = box.productList().size();

            for (InventoryRequest product : box.productList()) {

                InventoryLogCreateRequest log = new InventoryLogCreateRequest(
                        product.productId(),
                        box.productName(),
                        box.boxCode(),
                        request.transactionCode(),
                        product.productLogType(),
                        quantity,
                        request.supplyPrice(),
                        box.price(),
                        fromType,
                        request.fromLocationId(),
                        toType,
                        request.toLocationId(),
                        actorType,
                        request.fromLocationId());

                result.add(log);
            }
        }
        return result;
    }

}
