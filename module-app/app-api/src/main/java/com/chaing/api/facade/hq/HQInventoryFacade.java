package com.chaing.api.facade.hq;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.businessunits.repository.FranchiseRepository;
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
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.entity.HQInventory;
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
import com.chaing.domain.sales.dto.response.FranchiseSalesDailyQuantityResponse;
import com.chaing.domain.sales.service.FranchiseSalesService;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.service.UserManagementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.chaing.domain.inventories.enums.LocationType.FACTORY;
import static com.chaing.domain.inventories.enums.LocationType.FRANCHISE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQInventoryFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final InventoryLogService inventoryLogService;
    private final UserManagementService userManagementService;
    private final FranchiseRepository franchiseRepository;
    private final FranchiseSalesService franchiseSalesService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Map<Long, String> getFranchiseList() {
        return franchiseRepository.findAll().stream()
                .collect(Collectors.toMap(Franchise::getFranchiseId, Franchise::getName));
    }

    public List<HQInventoryProductResponse> getStock(StockSearchRequest request) {
        String key = "inv:hq:stock:%s:%s:%s".formatted(
                nullToDash(request.productCode()),
                nullToDash(request.name()),
                nullToDash(request.status()));

        List<HQInventoryProductResponse> cached = readListCache(
                key, new TypeReference<List<HQInventoryProductResponse>>() {});
        if (cached != null) return cached;

        List<ProductInfoResponse> products = productService.getInventoryProducts(request.productCode(), request.name());
        List<Long> ids = products.stream().map(ProductInfoResponse::productId).toList();
        Map<Long, InventoryProductInfoResponse> productInfos = inventoryService.getStock(ids, request.status());

        List<HQInventoryProductResponse> result = products.stream()
                .map(p -> {
                    InventoryProductInfoResponse info = productInfos.get(p.productId());
                    if (info == null) {
                        return new HQInventoryProductResponse(
                                p.productId(), p.productCode(), p.name(), 0,
                                p.productCode().substring(4, 6), 0, null);
                    }
                    return new HQInventoryProductResponse(
                            p.productId(), p.productCode(), p.name(),
                            info.totalQuantity(), p.productCode().substring(4, 6),
                            info.safetyStock(), info.status());
                })
                .toList();

        writeCache(key, result);
        return result;
    }

    public List<HQInventoryBatchResponse> getBatches(Long productId) {
        String key = "inv:hq:batches:%d".formatted(productId);
        List<HQInventoryBatchResponse> cached = readListCache(
                key, new TypeReference<List<HQInventoryBatchResponse>>() {});
        if (cached != null) return cached;

        List<HQInventoryBatchResponse> result = inventoryService.getBatches(productId);
        writeCache(key, result);
        return result;
    }

    public List<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request) {
        String key = "inv:hq:items:%d:%s:%s:%s:%s".formatted(
                request.productId(),
                nullToDash(request.serialCode()),
                request.manufactureDate() == null ? "-" : request.manufactureDate().toString(),
                request.shippedAt() == null ? "-" : request.shippedAt().toString(),
                request.receivedAt() == null ? "-" : request.receivedAt().toString());

        List<HQInventoryItemResponse> cached = readListCache(
                key, new TypeReference<List<HQInventoryItemResponse>>() {});
        if (cached != null) return cached;

        List<HQInventoryItemResponse> result = inventoryService.getItems(request);
        writeCache(key, result);
        return result;
    }

    public List<FranchiseInventoryProductResponse> getFranchiseStock(Long franchiseId, StockSearchRequest request) {
        String key = "inv:fr:stock:%d:%s:%s:%s".formatted(
                franchiseId,
                nullToDash(request.productCode()),
                nullToDash(request.name()),
                nullToDash(request.status()));

        List<FranchiseInventoryProductResponse> cached = readListCache(
                key, new TypeReference<List<FranchiseInventoryProductResponse>>() {});
        if (cached != null) return cached;

        List<ProductInfoResponse> products = productService.getInventoryProducts(request.productCode(), request.name());
        List<Long> ids = products.stream().map(ProductInfoResponse::productId).toList();

        Map<Long, InventoryProductInfoResponse> productInfos = inventoryService.getFranchiseStock(
                franchiseId, ids, request.status());

        List<FranchiseInventoryProductResponse> result = products.stream()
                .map(p -> {
                    InventoryProductInfoResponse info = productInfos.get(p.productId());
                    if (info == null) {
                        return new FranchiseInventoryProductResponse(
                                p.productId(), p.productCode(), p.name(), 0,
                                p.productCode().substring(4, 6), 0, null);
                    }
                    return new FranchiseInventoryProductResponse(
                            p.productId(), p.productCode(), p.name(),
                            info.totalQuantity(), p.productCode().substring(4, 6),
                            info.safetyStock(), info.status());
                })
                .toList();

        writeCache(key, result);
        return result;
    }

    public List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId) {
        String key = "inv:fr:batches:%d:%d".formatted(franchiseId, productId);
        List<FranchiseInventoryBatchResponse> cached = readListCache(
                key, new TypeReference<List<FranchiseInventoryBatchResponse>>() {});
        if (cached != null) return cached;

        List<FranchiseInventoryBatchResponse> result = inventoryService.getFranchiseBatches(franchiseId, productId);
        writeCache(key, result);
        return result;
    }

    public List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
                                                                  FranchiseInventoryItemsRequest request) {
        String key = "inv:fr:items:%d:%d:%s:%s:%s:%s:%s".formatted(
                franchiseId,
                request.productId(),
                nullToDash(request.serialCode()),
                nullToDash(request.boxCode()),
                request.manufactureDate() == null ? "-" : request.manufactureDate().toString(),
                request.shippedAt() == null ? "-" : request.shippedAt().toString(),
                request.receivedAt() == null ? "-" : request.receivedAt().toString());

        List<FranchiseInventoryItemResponse> cached = readListCache(
                key, new TypeReference<List<FranchiseInventoryItemResponse>>() {});
        if (cached != null) return cached;

        List<FranchiseInventoryItemResponse> result = inventoryService.getFranchiseItems(franchiseId, request);
        writeCache(key, result);
        return result;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void calculateSafetyStock() {
        String lockKey = "lock:safety-stock:refresh";
        String lockValue = UUID.randomUUID().toString();

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofMinutes(10));

        if (!Boolean.TRUE.equals(locked)) return;

        try {
            inventoryService.updateExpiredStatus();

            List<Long> franchiseIds = inventoryService.getAllFranchiseIds();
            List<Long> productIds = productService.getAllProductIds();

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(90);
            LocalDate endDate = today.minusDays(60);

            List<FranchiseSalesDailyQuantityResponse> salesRows =
                    franchiseSalesService.getDailyProductSalesForSafetyStock(
                            franchiseIds, productIds, startDate, endDate);

            List<ActorProductSalesResponse> salesData = toActorProductSales(salesRows);

            for (ActorProductSalesResponse franchise : salesData) {
                Long franchiseId = franchise.actorId();
                for (ProductSalesResponse product : franchise.products()) {
                    double stdDev = calculateStdDev(product.sales());
                    double z = 1.65;
                    int leadTime = 3;
                    int safetyStockInt = (int) Math.ceil(z * stdDev * Math.sqrt(leadTime));

                    inventoryService.updateSafetyStock(
                            FRANCHISE, franchiseId, product.productId(), safetyStockInt);
                }
            }

            List<Long> factoryIds = userManagementService.getBusinessUnitIdsByRole(UserRole.FACTORY);
            List<ActorProductSalesResponse> factoryOutboundData = inventoryLogService.getProductSales(
                    factoryIds, productIds, ActorType.FACTORY, LogType.OUTBOUND);

            for (ActorProductSalesResponse factory : factoryOutboundData) {
                Long actorId = factory.actorId();
                for (ProductSalesResponse product : factory.products()) {
                    double stdDev = calculateStdDev(product.sales());
                    double z = 1.65;
                    int leadTime = 3;
                    int safetyStockInt = (int) Math.ceil(z * stdDev * Math.sqrt(leadTime));

                    inventoryService.updateSafetyStock(
                            FACTORY, actorId, product.productId(), safetyStockInt);
                }
            }

            evictInventoryCache();
        } finally {
            String current = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    public InventoryAlertResponse getInventoryAlerts() {
        String key = "inv:hq:alerts";
        InventoryAlertResponse cached = readObjectCache(key, InventoryAlertResponse.class);
        if (cached != null) return cached;

        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("FACTORY", 1L);
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FACTORY", 1L);

        List<Long> ids = productService.getAllProductIds();
        Map<Long, ProductInfo> products = productService.getProductInfos(ids);

        List<SafetyStockAlertResponse> safetyStockAlerts = safetyStockAlert.stream()
                .filter(k -> products.containsKey(k.productId()))
                .map(k -> new SafetyStockAlertResponse(
                        products.get(k.productId()).productCode(),
                        products.get(k.productId()).productName(),
                        k.currentQuantity(),
                        k.safetyStock()))
                .toList();

        List<ExpirationAlertResponse> expirationAlert = expirationAlerts.stream()
                .filter(k -> products.containsKey(k.productId()))
                .map(k -> new ExpirationAlertResponse(
                        products.get(k.productId()).productName(),
                        k.manufactureDate(),
                        k.quantity(),
                        k.daysUntilExpiration()))
                .toList();

        InventoryAlertResponse result = InventoryAlertResponse.builder()
                .safetyStockAlerts(safetyStockAlerts)
                .expirationAlerts(expirationAlert)
                .build();

        writeCache(key, result);
        return result;
    }

    public InventoryAlertResponse getFranchiseInventoryAlerts(Long franchiseId) {
        String key = "inv:fr:alerts:%d".formatted(franchiseId);
        InventoryAlertResponse cached = readObjectCache(key, InventoryAlertResponse.class);
        if (cached != null) return cached;

        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("FRANCHISE", franchiseId);
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FRANCHISE", franchiseId);

        List<Long> ids = productService.getAllProductIds();
        Map<Long, ProductInfo> products = productService.getProductInfos(ids);

        List<SafetyStockAlertResponse> safetyStockAlerts = safetyStockAlert.stream()
                .filter(k -> products.containsKey(k.productId()))
                .map(k -> new SafetyStockAlertResponse(
                        products.get(k.productId()).productCode(),
                        products.get(k.productId()).productName(),
                        k.currentQuantity(),
                        k.safetyStock()))
                .toList();

        List<ExpirationAlertResponse> expirationAlert = expirationAlerts.stream()
                .filter(k -> products.containsKey(k.productId()))
                .map(k -> new ExpirationAlertResponse(
                        products.get(k.productId()).productName(),
                        k.manufactureDate(),
                        k.quantity(),
                        k.daysUntilExpiration()))
                .toList();

        InventoryAlertResponse result = InventoryAlertResponse.builder()
                .safetyStockAlerts(safetyStockAlerts)
                .expirationAlerts(expirationAlert)
                .build();

        writeCache(key, result);
        return result;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void increaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
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

        LocationType fromType = LocationType.valueOf(inventoryBatchRequest.fromLocationType().toUpperCase());
        List<String> serialCodes = convertsSerialCode(inventoryBatchRequest.boxes());

        if (fromType == LocationType.FRANCHISE) {
            inventoryService.deleteFranchiseInventory(inventoryBatchRequest.fromLocationId(), serialCodes);
        } else if (fromType == LocationType.FACTORY) {
            inventoryService.deleteFactoryInventory(serialCodes);
        } else {
            inventoryService.deleteHqInventory(serialCodes);
        }

        evictInventoryCache();
        return null;
    }

    public List<String> convertsSerialCode(List<InventoryBoxRequest> boxes) {
        return boxes.stream()
                .flatMap(box -> box.productList().stream())
                .map(InventoryRequest::serialCode)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void decreaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        List<String> serialCodes = convertsSerialCode(inventoryBatchRequest.boxes());
        inventoryService.updateShippingStatus(serialCodes);

        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        evictInventoryCache();
        return null;
    }

    private double calculateStdDev(List<DailySales> sales) {
        if (sales == null || sales.isEmpty()) return 0;

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

    private List<ActorProductSalesResponse> toActorProductSales(List<FranchiseSalesDailyQuantityResponse> rows) {
        Map<Long, Map<Long, List<DailySales>>> grouped = new HashMap<>();

        for (FranchiseSalesDailyQuantityResponse row : rows) {
            grouped.computeIfAbsent(row.franchiseId(), k -> new HashMap<>())
                    .computeIfAbsent(row.productId(), k -> new ArrayList<>())
                    .add(new DailySales(row.date(), row.quantity()));
        }

        return grouped.entrySet().stream()
                .map(actorEntry -> {
                    Long actorId = actorEntry.getKey();

                    List<ProductSalesResponse> products = actorEntry.getValue().entrySet().stream()
                            .map(productEntry -> {
                                List<DailySales> daily = productEntry.getValue();
                                int totalSales = daily.stream().mapToInt(DailySales::quantity).sum();
                                return new ProductSalesResponse(productEntry.getKey(), daily, totalSales);
                            })
                            .toList();

                    return new ActorProductSalesResponse(actorId, products);
                })
                .toList();
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
    public Void disposalInventory(DisposalRequest request, Long locationId) {
        String actorTypeRaw = request.actorType().toUpperCase();
        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        if (actorTypeRaw.equals("HQ")) {
            List<HQInventory> inventories = inventoryService.getHqInventoriesByIds(request.inventoryIds());
            List<Long> productIds = inventories.stream().map(HQInventory::getProductId).distinct().toList();
            Map<Long, ProductInfo> productInfos = productIds.isEmpty() ? Map.of() : productService.getProductInfos(productIds);

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
            List<Long> productIds = inventories.stream().map(FactoryInventory::getProductId).distinct().toList();
            Map<Long, ProductInfo> productInfos = productIds.isEmpty() ? Map.of() : productService.getProductInfos(productIds);

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
                        locationId,
                        null,
                        null,
                        ActorType.FACTORY,
                        request.actorId()));
            }
        } else if (actorTypeRaw.equals("FRANCHISE")) {
            List<FranchiseInventory> inventories = inventoryService.getFranchiseInventoriesByIds(request.inventoryIds());
            List<Long> productIds = inventories.stream().map(FranchiseInventory::getProductId).distinct().toList();
            Map<Long, ProductInfo> productInfos = productIds.isEmpty() ? Map.of() : productService.getProductInfos(productIds);

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
        evictInventoryCache();
        return null;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void setSafetyStock(SafetyStockRequest request) {
        inventoryService.setSafetyStock(request);
        evictInventoryCache();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void resetSafetyStock(Long locationId, Long productId) {
        inventoryService.resetSafetyStockToDefault("FACTORY", locationId, productId);
        evictInventoryCache();
    }

    public boolean verifyAdminPassword(Long userId, String password) {
        return userManagementService.verifyPassword(userId, password);
    }

    private String nullToDash(String value) {
        return value == null ? "-" : value;
    }

    private <T> List<T> readListCache(String key, TypeReference<List<T>> typeRef) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;
            return objectMapper.readValue(cached, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T readObjectCache(String key, Class<T> clazz) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;
            return objectMapper.readValue(cached, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeCache(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), CACHE_TTL);
        } catch (Exception ignored) {
        }
    }

    private void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    private void evictInventoryCache() {
        evictByPattern("inv:hq:*");
        evictByPattern("inv:fr:*");
    }
}
