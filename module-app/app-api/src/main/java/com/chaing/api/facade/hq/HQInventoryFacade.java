package com.chaing.api.facade.hq;

import com.chaing.api.config.RedisCacheHelper;
import com.chaing.api.facade.notification.NotificationFacade;
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
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.event.NotificationEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.chaing.domain.inventories.enums.LocationType.FACTORY;
import static com.chaing.domain.inventories.enums.LocationType.FRANCHISE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQInventoryFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final Long DEFAULT_FACTORY_ID = 1L;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );
    private static final long FACTORY_STOCK_ALERT_TARGET_BASE = 2_000_000_000L;
    private static final long FRANCHISE_STOCK_ALERT_TARGET_BASE = 3_000_000_000L;
    private static final long TARGET_LOCATION_MULTIPLIER = 1_000_000L;

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final InventoryLogService inventoryLogService;
    private final UserManagementService userManagementService;
    private final FranchiseRepository franchiseRepository;
    private final FranchiseSalesService franchiseSalesService;
    private final RedisCacheHelper redisCacheHelper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;
    private final ApplicationEventPublisher eventPublisher;

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
                            info.safetyStock() != null ? info.safetyStock() : 0,
                            info.status());
                })
                .toList();

        writeCache(key, result);
        return result;
    }

    public Page<HQInventoryBatchResponse> getBatches(Long productId, Pageable pageable) {
        String key = "inv:hq:batches:%d:%s".formatted(productId, pageableKey(pageable));
        Page<HQInventoryBatchResponse> cached = readPageCache(key, HQInventoryBatchResponse.class, pageable);
        if (cached != null) return cached;

        Page<HQInventoryBatchResponse> result = inventoryService.getBatches(productId, pageable);
        writePageCache(key, result);
        return result;
    }

    public Page<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request, Pageable pageable) {
        String key = "inv:hq:items:%d:%s:%s:%s:%s:%s".formatted(
                request.productId(),
                nullToDash(request.serialCode()),
                request.manufactureDate() == null ? "-" : request.manufactureDate().toString(),
                request.shippedAt() == null ? "-" : request.shippedAt().toString(),
                request.receivedAt() == null ? "-" : request.receivedAt().toString(),
                pageableKey(pageable));

        Page<HQInventoryItemResponse> cached = readPageCache(key, HQInventoryItemResponse.class, pageable);
        if (cached != null) return cached;

        Page<HQInventoryItemResponse> result = inventoryService.getItems(request, pageable);
        writePageCache(key, result);
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

    public Page<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId, Pageable pageable) {
        String key = "inv:fr:batches:%d:%d:%s".formatted(franchiseId, productId, pageableKey(pageable));
        Page<FranchiseInventoryBatchResponse> cached = readPageCache(key, FranchiseInventoryBatchResponse.class, pageable);
        if (cached != null) return cached;

        Page<FranchiseInventoryBatchResponse> result = inventoryService.getFranchiseBatches(franchiseId, productId, pageable);
        writePageCache(key, result);
        return result;
    }

    public Page<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId,
                                                                  FranchiseInventoryItemsRequest request,
                                                                  Pageable pageable) {
        String key = "inv:fr:items:%d:%d:%s:%s:%s:%s:%s:%s".formatted(
                franchiseId,
                request.productId(),
                nullToDash(request.serialCode()),
                nullToDash(request.boxCode()),
                request.manufactureDate() == null ? "-" : request.manufactureDate().toString(),
                request.shippedAt() == null ? "-" : request.shippedAt().toString(),
                request.receivedAt() == null ? "-" : request.receivedAt().toString(),
                pageableKey(pageable));

        Page<FranchiseInventoryItemResponse> cached = readPageCache(key, FranchiseInventoryItemResponse.class, pageable);
        if (cached != null) return cached;

        Page<FranchiseInventoryItemResponse> result = inventoryService.getFranchiseItems(franchiseId, request, pageable);
        writePageCache(key, result);
        return result;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
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
            if (factoryIds == null || factoryIds.isEmpty()) {
                publishStockAlert("FACTORY", DEFAULT_FACTORY_ID);
            } else {
                factoryIds.forEach(id -> publishStockAlert("FACTORY", id));
            }
            franchiseIds.forEach(id -> publishStockAlert("FRANCHISE", id));
        } finally {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), lockValue);
        }
    }

    public InventoryAlertResponse getInventoryAlerts() {
        String key = "inv:hq:alerts";
        InventoryAlertResponse cached = readObjectCache(key, InventoryAlertResponse.class);
        if (cached != null) return cached;

        List<SafetyStockResponse> safetyStockAlert = inventoryService.getLowStockAlerts("FACTORY", DEFAULT_FACTORY_ID);
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FACTORY", DEFAULT_FACTORY_ID);

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

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
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

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
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
            int quantity = 1;

            for (InventoryRequest product : box.productList()) {
                InventoryLogCreateRequest log = new InventoryLogCreateRequest(
                        product.productId(),
                        box.productName(),
                        box.boxCode(),
                        request.transactionCode(),
                        product.productLogType(),
                        quantity,
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

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void disposalInventory(DisposalRequest request, Long locationId) {
        String actorTypeRaw = request.actorType().toUpperCase();

        // 1) 사용자가 선택한 재고를 boxCode 기준으로 전체 확장
        List<Long> expandedIds = inventoryService.expandInventoryIdsByBoxCode(
                actorTypeRaw,
                request.inventoryIds(),
                locationId,
                request.actorId()
        );

        if (expandedIds.isEmpty()) {
            return null;
        }

        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        // 2) 확장된 대상 기준으로 로그 생성
        if (actorTypeRaw.equals("HQ")) {
            List<HQInventory> inventories = inventoryService.getHqInventoriesByIds(expandedIds);
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
                        LocationType.HQ,
                        request.actorId(),
                        null,
                        null,
                        ActorType.HQ,
                        request.actorId()
                ));
            }

        } else if (actorTypeRaw.equals("FACTORY")) {
            List<FactoryInventory> inventories = inventoryService.getFactoryInventoriesByIds(expandedIds);
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
                        LocationType.FACTORY,
                        locationId,
                        null,
                        null,
                        ActorType.FACTORY,
                        request.actorId()
                ));
            }

        } else if (actorTypeRaw.equals("FRANCHISE")) {
            List<FranchiseInventory> inventories = inventoryService.getFranchiseInventoriesByIds(expandedIds);
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
                        LocationType.FRANCHISE,
                        inv.getFranchiseId(),
                        null,
                        null,
                        ActorType.FRANCHISE,
                        inv.getFranchiseId()
                ));
            }

        } else {
            throw new IllegalArgumentException("Unsupported actorType: " + request.actorType());
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }

        // 3) 확장된 ID 전체 삭제
        inventoryService.disposalInventoryByIds(actorTypeRaw, expandedIds, locationId, request.actorId());

        evictInventoryCache();
        return null;
    }


    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void setSafetyStock(SafetyStockRequest request) {
        inventoryService.setSafetyStock(request);
        evictInventoryCache();
        publishStockAlert(request.locationType(), request.locationId());
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void resetSafetyStock(Long locationId, Long productId) {
        inventoryService.resetSafetyStockToDefault("FACTORY", locationId, productId);
        evictInventoryCache();
        publishStockAlert("FACTORY", locationId);
    }

    private String nullToDash(String value) {
        return value == null ? "-" : value;
    }

    private void publishStockAlert(String locationTypeRaw, Long locationId) {
        if (locationTypeRaw == null || locationId == null) return;

        String locationType = locationTypeRaw.toUpperCase();
        if (!"FACTORY".equals(locationType) && !"FRANCHISE".equals(locationType)) return;

        List<SafetyStockResponse> safetyAlerts = inventoryService.getLowStockAlerts(locationType, locationId);
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts(locationType, locationId);
        List<Long> recipientUserIds = resolveRecipientUserIds(locationType, locationId);
        if (recipientUserIds.isEmpty()) return;

        int lowStockCount = safetyAlerts.size();
        int expirationCount = expirationAlerts.size();
        String subject = "FACTORY".equals(locationType)
                ? "본사(공장)"
                : "가맹점(" + resolveFranchiseName(locationId) + ")";
        String message = "[재고 알림] %s - 안전재고 부족 %d건, 유통기한 임박 %d건"
                .formatted(subject, lowStockCount, expirationCount);

        for (Long userId : recipientUserIds) {
            long targetId = resolveUserStockAlertTargetId(locationType, locationId, userId);
            notificationFacade.deleteNotificationsByTarget(NotificationType.STOCK, targetId);

            if (lowStockCount == 0 && expirationCount == 0) {
                continue;
            }

            eventPublisher.publishEvent(NotificationEvent.ofUser(
                    userId,
                    NotificationType.STOCK,
                    message,
                    targetId
            ));
        }
    }

    private List<Long> resolveRecipientUserIds(String locationType, Long locationId) {
        if ("FRANCHISE".equals(locationType)) {
            return userManagementService.getActiveUserIdsByRoleAndBusinessUnitId(UserRole.FRANCHISE, locationId);
        }

        List<Long> locationUsers = userManagementService.getActiveUserIdsByRoleAndBusinessUnitId(UserRole.FACTORY, locationId);
        List<Long> hqUsers = userManagementService.getActiveUserIdsByRole(UserRole.HQ);
        return java.util.stream.Stream.concat(locationUsers.stream(), hqUsers.stream())
                .distinct()
                .toList();
    }

    private long resolveUserStockAlertTargetId(String locationType, Long locationId, Long userId) {
        if ("FRANCHISE".equals(locationType)) {
            return FRANCHISE_STOCK_ALERT_TARGET_BASE + (locationId * TARGET_LOCATION_MULTIPLIER) + userId;
        }
        return FACTORY_STOCK_ALERT_TARGET_BASE + (locationId * TARGET_LOCATION_MULTIPLIER) + userId;
    }

    private String resolveFranchiseName(Long franchiseId) {
        if (franchiseId == null) {
            return "-";
        }

        return franchiseRepository.findByFranchiseIdAndDeletedAtIsNull(franchiseId)
                .map(Franchise::getName)
                .orElse("ID:" + franchiseId);
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

    private void evictInventoryCache() {
        redisCacheHelper.evictByPattern("inv:hq:*");
        redisCacheHelper.evictByPattern("inv:fr:*");
    }

    private String pageableKey(Pageable pageable) {
        return "%d:%d:%s".formatted(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort().toString().replace(" ", "") : "-");
    }

    private <T> Page<T> readPageCache(String key, Class<T> itemClass, Pageable pageable) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null) return null;

            JsonNode root = objectMapper.readTree(cached);
            List<T> content = objectMapper.readerForListOf(itemClass).readValue(root.path("content"));
            long totalElements = root.path("totalElements").asLong(content.size());

            return new PageImpl<>(content, pageable, totalElements);
        } catch (Exception e) {
            return null;
        }
    }

    private void writePageCache(String key, Page<?> page) {
        try {
            Map<String, Object> payload = Map.of(
                    "content", page.getContent(),
                    "totalElements", page.getTotalElements());
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(payload), CACHE_TTL);
        } catch (Exception ignored) {
        }
    }
}
