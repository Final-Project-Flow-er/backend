package com.chaing.api.facade.hq;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.entity.HQInventory;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.request.InventoryBatchRequest;
import com.chaing.domain.inventories.dto.request.InventoryBoxRequest;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.dto.request.SafetyStockRequest;
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
import com.chaing.domain.inventorylogs.dto.response.ActorProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.DailySales;
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

import static com.chaing.domain.inventories.enums.LocationType.FACTORY;
import static com.chaing.domain.inventories.enums.LocationType.FRANCHISE;

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

    // 안전재고, 유통기한 계산
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void calculateSafetyStock() {
        // 0. 유통기한 만료 상태 업데이트
        inventoryService.updateExpiredStatus();

        List<Long> franchiseIds = inventoryService.getAllFranchiseIds();
        List<Long> ids = productService.getAllProductIds();

        // 1. 가맹점 안전재고 계산 (판매량 기준)
        List<ActorProductSalesResponse> salesData = inventoryLogService.getProductSales(
                franchiseIds, ids, ActorType.FRANCHISE, LogType.SALE);

        for (ActorProductSalesResponse franchise : salesData) {
            Long franchiseId = franchise.actorId();
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

        // 2. 공장(FACTORY) 안전재고 계산 (출고량 기준)
        List<ActorProductSalesResponse> factoryOutboundData = inventoryLogService.getProductSales(
                null, ids, ActorType.FACTORY, LogType.OUTBOUND);

        for (ActorProductSalesResponse factory : factoryOutboundData) {
            Long actorId = factory.actorId(); // null일 것임
            for (ProductSalesResponse product : factory.products()) {
                double stdDev = calculateStdDev(product.sales());
                double z = 1.65; // 95% 서비스 수준
                int leadTime = 3; // 예시 리드타임
                double safetyStock = z * stdDev * Math.sqrt(leadTime);
                int safetyStockInt = (int) Math.ceil(safetyStock);

                inventoryService.updateSafetyStock(
                        FACTORY,
                        actorId,
                        product.productId(),
                        safetyStockInt);
            }
        }
    }

    // 안전재고 유통기한 알림
    public InventoryAlertResponse getInventoryAlerts() {
        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("FACTORY", null);

        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FACTORY", null);

        List<Long> ids = productService.getAllProductIds();

        Map<Long, ProductInfo> products = productService.getProductInfos(ids);

        // 코드와 이름 조합
        List<SafetyStockAlertResponse> safetyStockAlerts = safetyStockAlert.stream()
                .filter(k -> products.containsKey(k.productId()))
                .map(k -> new SafetyStockAlertResponse(
                        products.get(k.productId()).productCode(),
                        products.get(k.productId()).productName(),
                        k.currentQuantity(),
                        k.safetyStock()))
                .toList();

        // 코드와 이름 조합
        List<ExpirationAlertResponse> expirationAlert = expirationAlerts.stream()
                .filter(k -> products.containsKey(k.productId()))
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

        // 재고 감소 인벤토리Ids로 받음
        if (toType == LocationType.FRANCHISE) {
            inventoryService.franchiseIncreaseInventory(inventoryBatchRequest);
        } else if (toType == LocationType.FACTORY) {
            inventoryService.factoryIncreaseInventory(inventoryBatchRequest);
        } else {
            inventoryService.hqIncreaseInventory(inventoryBatchRequest);
        }

        // 해당 재고 삭제
        LocationType fromType = LocationType.valueOf(inventoryBatchRequest.fromLocationType().toUpperCase());

        List<String> serialCodes = convertsSerialCode(inventoryBatchRequest.boxes());

        if (fromType == LocationType.FRANCHISE) {
            inventoryService.deleteFranchiseInventory(inventoryBatchRequest.fromLocationId(), serialCodes);
        } else if (fromType == LocationType.FACTORY) {
            inventoryService.deleteFactoryInventory(serialCodes);
        } else {
            inventoryService.deleteHqInventory(serialCodes);
        }
        return null;
    }

    // 제품 식별코드 반환
    public List<String> convertsSerialCode(List<InventoryBoxRequest> boxes) {
        return boxes.stream()
                .flatMap(box -> box.productList().stream())
                .map(InventoryRequest::serialCode)
                .toList();
    }

    // 재고 상태 변환 및 로그 기록
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void decreaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        List<String> serialCodes = convertsSerialCode(inventoryBatchRequest.boxes());
        inventoryService.updateShippingStatus(serialCodes);
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void disposalInventory(DisposalRequest request) {
        String actorTypeRaw = request.actorType().toUpperCase();
        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        if (actorTypeRaw.equals("HQ")) {
            List<HQInventory> inventories = inventoryService.getHqInventoriesByIds(request.inventoryIds());
            Map<Long, ProductInfo> productInfos = productService.getProductInfos(
                    inventories.stream().map(HQInventory::getProductId).distinct().toList());

            for (HQInventory inv : inventories) {
                ProductInfo pInfo = productInfos.get(inv.getProductId());
                logs.add(new InventoryLogCreateRequest(
                        inv.getProductId(),
                        pInfo != null ? pInfo.productName() : "Unknown",
                        inv.getBoxCode(),
                        null,
                        LogType.DISPOSAL,
                        1,
                        pInfo != null ? pInfo.tradePrice() : null,
                        pInfo != null ? pInfo.retailPrice() : null,
                        LocationType.HQ,
                        request.actorId(),
                        null,
                        null,
                        ActorType.HQ,
                        request.actorId()));
            }
        } else if (actorTypeRaw.equals("FACTORY")) {
            List<FactoryInventory> inventories = inventoryService.getFactoryInventoriesByIds(request.inventoryIds());
            Map<Long, ProductInfo> productInfos = productService.getProductInfos(
                    inventories.stream().map(FactoryInventory::getProductId).distinct().toList());

            for (FactoryInventory inv : inventories) {
                ProductInfo pInfo = productInfos.get(inv.getProductId());
                logs.add(new InventoryLogCreateRequest(
                        inv.getProductId(),
                        pInfo != null ? pInfo.productName() : "Unknown",
                        inv.getBoxCode(),
                        null,
                        LogType.DISPOSAL,
                        1,
                        pInfo != null ? pInfo.tradePrice() : null,
                        pInfo != null ? pInfo.retailPrice() : null,
                        LocationType.FACTORY,
                        request.actorId(),
                        null,
                        null,
                        ActorType.FACTORY,
                        request.actorId()));
            }
        } else if (actorTypeRaw.equals("FRANCHISE")) {
            List<FranchiseInventory> inventories = inventoryService
                    .getFranchiseInventoriesByIds(request.inventoryIds());
            Map<Long, ProductInfo> productInfos = productService.getProductInfos(
                    inventories.stream().map(FranchiseInventory::getProductId).distinct().toList());

            for (FranchiseInventory inv : inventories) {
                ProductInfo pInfo = productInfos.get(inv.getProductId());
                logs.add(new InventoryLogCreateRequest(
                        inv.getProductId(),
                        pInfo != null ? pInfo.productName() : "Unknown",
                        inv.getBoxCode(),
                        null,
                        LogType.DISPOSAL,
                        1,
                        pInfo != null ? pInfo.tradePrice() : null,
                        pInfo != null ? pInfo.retailPrice() : null,
                        LocationType.FRANCHISE,
                        inv.getFranchiseId(),
                        null,
                        null,
                        ActorType.FRANCHISE,
                        inv.getFranchiseId()));
            }
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }

        inventoryService.disposalInventory(request);
        return null;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void setSafetyStock(SafetyStockRequest request) {
        inventoryService.setSafetyStock(request);
    }
}
