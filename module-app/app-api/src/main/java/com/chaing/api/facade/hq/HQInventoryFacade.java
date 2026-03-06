package com.chaing.api.facade.hq;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryBoxRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.dto.request.StockSearchRequest;
import com.chaing.domain.inventories.dto.response.ExpirationAlertResponse;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryProductResponse;
import com.chaing.domain.inventories.dto.response.InventoryAlertResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockAlertResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.response.DailySales;
import com.chaing.domain.inventorylogs.dto.response.FranchiseProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.ProductSalesResponse;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import com.chaing.domain.products.dto.response.ProductInfoResponse;
import com.chaing.domain.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.chaing.domain.inventories.enums.LocationType.FRANCHISE;
import static com.chaing.domain.inventories.enums.LocationType.HQ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQInventoryFacade {
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final InventoryLogService inventoryLogService;

    // 대분류
    public List<HQInventoryProductResponse> getStock(StockSearchRequest request) {
        // id, code, name 반환(재고에 코드랑 이름이 없으므로 가지고 오기)
        List<ProductInfoResponse> products = productService.getInventoryProducts(request.productCode(), request.name());

        List<Long> ids = products.stream()
                .map(ProductInfoResponse::productId)
                .toList();

        Map<Long, InventoryProductInfoResponse> productInfos = inventoryService.getStock(ids, request.status());
        return products.stream()
                .map(p -> {

                    InventoryProductInfoResponse info = productInfos.get(p.productId());

                    if (info == null) {
                        return new HQInventoryProductResponse(
                                p.productId(),
                                p.productCode(),
                                p.name(),
                                0,
                                p.productCode().substring(4, 6),
                                0,
                                null);
                    }

                    return new HQInventoryProductResponse(
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

    // 중분류
    public List<HQInventoryBatchResponse> getBatches(Long productId) {
        return inventoryService.getBatches(productId);
    }

    // 소분류
    public List<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request) {
        return inventoryService.getItems(request);
    }

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

    // 특정 가맹정 중분류
    public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
        return inventoryService.getFranchiseBatches(franchiseId, productId);
    }

    // 특정 가맹점 소분류
    public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
            FranchiseInventoryItemsRequest request) {
        return inventoryService.getFranchiseItems(franchiseId, request);
    }

    // 안전재고 계산
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void calculateSafetyStock() {
        List<Long> franchiseIds = inventoryService.getAllFranchiseIds();
        List<Long> ids = productService.getAllProductIds();

        // 1. 가맹점 안전재고 계산 (판매량 기준)
        List<FranchiseProductSalesResponse> salesData = inventoryLogService.getProductSales(
                franchiseIds, ids, ActorType.FRANCHISE, LogType.SALE);

        for (FranchiseProductSalesResponse franchise : salesData) {
            Long franchiseId = franchise.franchiseId();
            for (ProductSalesResponse product : franchise.products()) {
                double stdDev = calculateStdDev(product.sales());
                double z = 1.65; // 95% 서비스 수준
                int leadTime = 3; // 예시 리드타임
                double safetyStock = z * stdDev * Math.sqrt(leadTime);
                int safetyStockInt = (int) Math.ceil(safetyStock);

                inventoryService.updateSafetyStock(
                        FRANCHISE,
                        franchiseId,
                        product.productId(),
                        safetyStockInt);
            }
        }

        // 2. 본사(HQ) 안전재고 계산 (출고량 기준)
        List<FranchiseProductSalesResponse> hqOutboundData = inventoryLogService.getProductSales(
                List.of(1L), ids, ActorType.HQ, LogType.OUTBOUND);

        for (FranchiseProductSalesResponse hq : hqOutboundData) {
            // 본사 ID는 1L
            Long hqId = hq.franchiseId();
            for (ProductSalesResponse product : hq.products()) {
                double stdDev = calculateStdDev(product.sales());
                double z = 1.65; // 95% 서비스 수준
                int leadTime = 3; // 예시 리드타임
                double safetyStock = z * stdDev * Math.sqrt(leadTime);
                int safetyStockInt = (int) Math.ceil(safetyStock);

                inventoryService.updateSafetyStock(
                        HQ,
                        hqId,
                        product.productId(),
                        safetyStockInt);
            }
        }
    }

    // 안전재고 유통기한 알림
    public InventoryAlertResponse getInventoryAlerts() {
        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("HQ", 1L); // 임의로 본사 이렇게 정함

        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("HQ", 1L);

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

    // 재고 증가 및 로그 기록
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void increaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        // 여기서 받을땐 로그가 가용상태에서 받아야 할 듯
        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        LocationType toType = LocationType.valueOf(inventoryBatchRequest.toLocationType().toUpperCase());

        if (toType == LocationType.FRANCHISE) {
            inventoryService.franchiseIncreaseInventory(inventoryBatchRequest);
        } else if (toType == LocationType.FACTORY) {
            inventoryService.factoryIncreaseInventory(inventoryBatchRequest);
        } else {
            inventoryService.hqIncreaseInventory(inventoryBatchRequest);
        }

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

    // 재고 상태 변환 및 로그 기록
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void decreaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {

        inventoryService.updateShippingStatus(inventoryBatchRequest.boxes());
        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        return null;
    }

    // 안전재고 계산
    public double calculateStdDev(List<DailySales> sales) {

        double avg = sales.stream()
                .mapToInt(DailySales::quantity)
                .average()
                .orElse(0);

        double variance = sales.stream()
                .mapToDouble(s -> Math.pow(s.quantity() - avg, 2))
                .average()
                .orElse(0);

        return Math.sqrt(variance);
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
