package com.chaing.api.facade.franchise;

import com.chaing.api.facade.notification.NotificationFacade;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.DisposalRequest;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
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
import com.chaing.domain.inventories.dto.response.InventoryAlertResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockAlertResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.enums.LocationType;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import com.chaing.domain.notifications.enums.NotificationType;
import com.chaing.domain.notifications.event.NotificationEvent;
import com.chaing.domain.products.dto.response.ProductInfoResponse;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.service.UserManagementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FranchiseInventoryFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final long FRANCHISE_STOCK_ALERT_TARGET_BASE = 3_000_000_000L;
    private static final long TARGET_LOCATION_MULTIPLIER = 1_000_000L;

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final InventoryLogService inventoryLogService;
    private final UserManagementService userManagementService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationFacade notificationFacade;
    private final ApplicationEventPublisher eventPublisher;

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
        Map<Long, InventoryProductInfoResponse> productInfos = inventoryService.getFranchiseStock(franchiseId, ids, request.status());

        List<FranchiseInventoryProductResponse> result = products.stream()
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void increaseInventory(@Valid InventoryBatchRequest inventoryBatchRequest) {
        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        inventoryService.franchiseIncreaseInventory(inventoryBatchRequest);

        LocationType fromType = LocationType.valueOf(inventoryBatchRequest.fromLocationType().toUpperCase());
        List<String> serialCode = convertsSerialCode(inventoryBatchRequest.boxes());

        if (fromType == LocationType.FRANCHISE) {
            inventoryService.deleteFranchiseInventory(inventoryBatchRequest.fromLocationId(), serialCode);
        } else if (fromType == LocationType.FACTORY) {
            inventoryService.deleteFactoryInventory(serialCode);
        } else {
            inventoryService.deleteHqInventory(serialCode);
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
        inventoryService.updateFranchiseShippingStatus(inventoryBatchRequest.fromLocationId(), serialCodes);

        List<InventoryLogCreateRequest> logs = convert(inventoryBatchRequest);
        inventoryLogService.recordInventoryLog(logs);

        evictInventoryCache();
        return null;
    }

    @Transactional(readOnly = true)
    public InventoryAlertResponse getInventoryAlerts(Long franchiseId) {
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

    public List<InventoryLogCreateRequest> convert(InventoryBatchRequest request) {
        LocationType fromType = LocationType.valueOf(request.fromLocationType().toUpperCase());
        LocationType toType = LocationType.valueOf(request.toLocationType().toUpperCase());
        ActorType actorType = ActorType.valueOf(request.fromLocationType().toUpperCase());

        List<InventoryLogCreateRequest> result = new ArrayList<>();

        for (InventoryBoxRequest box : request.boxes()) {
            int quantity = 1; // 박스 단위 로그

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



    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Void disposalInventory(DisposalRequest request, Long locationId) {
        String actorTypeRaw = request.actorType().toUpperCase();
        List<InventoryLogCreateRequest> logs = new ArrayList<>();

        List<Long> expandedIds = inventoryService.expandInventoryIdsByBoxCode(
                actorTypeRaw,
                request.inventoryIds(),
                locationId,
                locationId
        );

        if (expandedIds.isEmpty()) {
            return null;
        }

        if (actorTypeRaw.equals("FRANCHISE")) {
            List<FranchiseInventory> inventories = inventoryService.getFranchiseInventoriesByIds(expandedIds);
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
                        LocationType.FRANCHISE,
                        locationId,
                        null,
                        null,
                        ActorType.FRANCHISE,
                        locationId
                ));
            }
        }

        if (!logs.isEmpty()) {
            inventoryLogService.recordInventoryLog(logs);
        }

        inventoryService.disposalInventoryByIds(actorTypeRaw, expandedIds, locationId, locationId);
        evictInventoryCache();
        return null;
    }



    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void setSafetyStock(SafetyStockRequest request) {
        inventoryService.setSafetyStock(request);
        evictInventoryCache();
        publishFranchiseStockAlert(request.locationId());
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void resetSafetyStock(Long franchiseId, Long productId) {
        inventoryService.resetSafetyStockToDefault("FRANCHISE", franchiseId, productId);
        evictInventoryCache();
        publishFranchiseStockAlert(franchiseId);
    }

    public boolean verifyAdminPassword(Long userId, String password) {
        return userManagementService.verifyPassword(userId, password);
    }

    private String nullToDash(String value) {
        return value == null ? "-" : value;
    }

    private void publishFranchiseStockAlert(Long franchiseId) {
        if (franchiseId == null) return;

        List<SafetyStockResponse> safetyAlerts = inventoryService.getLowStockAlerts("FRANCHISE", franchiseId);
        List<ExpirationBatchResultResponse> expirationAlerts = inventoryService.getExpirationAlerts("FRANCHISE", franchiseId);
        List<Long> recipientUserIds = userManagementService
                .getActiveUserIdsByRoleAndBusinessUnitId(UserRole.FRANCHISE, franchiseId);
        if (recipientUserIds.isEmpty()) return;

        int lowStockCount = safetyAlerts.size();
        int expirationCount = expirationAlerts.size();
        String message = "[재고 알림] 가맹점(" + franchiseId + ") - 안전재고 부족 "
                + lowStockCount + "건, 유통기한 임박 " + expirationCount + "건";

        for (Long userId : recipientUserIds) {
            long targetId = FRANCHISE_STOCK_ALERT_TARGET_BASE + (franchiseId * TARGET_LOCATION_MULTIPLIER) + userId;
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
