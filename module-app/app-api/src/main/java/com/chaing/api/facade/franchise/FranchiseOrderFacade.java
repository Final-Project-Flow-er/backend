package com.chaing.api.facade.franchise;

import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.core.dto.command.FranchiseOrderCodeAndQuantityCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.core.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderCreateResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderDetailResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemDetailResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderUpdateResponse;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.service.FranchiseOrderCodeGenerator;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemProjection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FranchiseOrderFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final String STOCK_LOCK_KEY = "lock:stock:check";
    private static final Duration STOCK_LOCK_TTL = Duration.ofSeconds(2);

    private final FranchiseOrderService franchiseOrderService;
    private final UserManagementService userManagementService;
    private final ProductService productService;
    private final FranchiseOrderCodeGenerator generator;
    private final FranchiseServiceImpl franchiseService;
    private final InventoryService inventoryService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 가맹점 발주 조회
    public List<FranchiseOrderResponse> getAllOrders(Long userId) {
        String cacheKey = "ord:fr:all:%d".formatted(userId);
        List<FranchiseOrderResponse> cached = readListCache(cacheKey, new TypeReference<>() {});
        if (cached != null) return cached;

        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);
        log.info("franchise id: {}", franchiseId);
        log.info("user id: {}", userId);

        // username
        String username = userManagementService.getUsernameByUserId(userId);

        // Map<orderId, FranchiseOrderCommand>
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.getAllOrdersByFranchiseIdAndUserId(franchiseId, userId);
        log.info("orders: {}", orders);

        // List<orderId>
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderId = franchiseOrderService.getOrderItemsByOrderIds(orderIds);
        log.info("orderItemByOrderId: {}", orderItemByOrderId);

        // List<productId>
        List<Long> productIds = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::productId)
                .distinct()
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        List<FranchiseOrderResponse> result = orders.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    FranchiseOrderCommand orderCommand = entry.getValue();
                    List<FranchiseOrderItemCommand> items = orderItemByOrderId.get(orderId);

                    if (items == null || items.isEmpty()) {
                        throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
                    }

                    return items.stream().map(item -> {
                        ProductInfo productInfo = productInfoByProductId.get(item.productId());

                        if (productInfo == null) {
                            throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                        }

                        return FranchiseOrderResponse.builder()
                                .orderCode(orderCommand.orderCode())
                                .orderStatus(orderCommand.orderStatus())
                                .productCode(productInfo.productCode())
                                .quantity(item.quantity())
                                .unitPrice(item.unitPrice())
                                .totalPrice(orderCommand.totalPrice())
                                .requestedDate(orderCommand.requestedDate())
                                .receiver(username)
                                .deliveryDate(orderCommand.deliveryDate())
                                .build();
                    });
                })
                .toList();
        writeCache(cacheKey, result);
        return result;
    }

    // 가맹점 발주 페이지네이션 조회 (아이템 행 단위)
    public Page<FranchiseOrderResponse> getAllOrdersPaged(Long userId, Pageable pageable) {
        String cacheKey = "ord:fr:page:%d:%s".formatted(userId, pageableKey(pageable));
        Page<FranchiseOrderResponse> cached = readPageCache(cacheKey, FranchiseOrderResponse.class, pageable);
        if (cached != null) return cached;

        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);
        String username = userManagementService.getUsernameByUserId(userId);

        Page<FranchiseOrderItemProjection> page =
                franchiseOrderService.getOrderItemPage(franchiseId, userId, pageable);

        if (page.isEmpty()) {
            Page<FranchiseOrderResponse> empty = new PageImpl<>(List.of(), pageable, 0);
            writePageCache(cacheKey, empty);
            return empty;
        }

        List<Long> productIds = page.getContent().stream()
                .map(FranchiseOrderItemProjection::productId).distinct().toList();
        Map<Long, ProductInfo> productInfoMap = productService.getProductInfos(productIds);

        List<FranchiseOrderResponse> content = page.getContent().stream()
                .map(p -> FranchiseOrderResponse.builder()
                        .orderCode(p.orderCode())
                        .orderStatus(p.orderStatus())
                        .productCode(productInfoMap.containsKey(p.productId())
                                ? productInfoMap.get(p.productId()).productCode() : null)
                        .quantity(p.quantity())
                        .unitPrice(p.unitPrice())
                        .totalPrice(p.totalPrice())
                        .requestedDate(p.requestedDate())
                        .receiver(username)
                        .deliveryDate(p.deliveryDate())
                        .build())
                .toList();

        Page<FranchiseOrderResponse> result = new PageImpl<>(content, pageable, page.getTotalElements());
        writePageCache(cacheKey, result);
        return result;
    }

    // 가맹점의 발주 번호에 따른 특정 발주 조회
    public FranchiseOrderDetailResponse getOrder(Long userId, String orderCode) {
        String cacheKey = "ord:fr:detail:%d:%s".formatted(userId, orderCode);
        FranchiseOrderDetailResponse cached = readObjectCache(cacheKey, FranchiseOrderDetailResponse.class);
        if (cached != null) return cached;

        // userRole 확인
        String userRole = userManagementService.getUserById(userId).getRole().toString();

        Long franchiseId;
        Long orderUserId;
        String username;
        String phoneNumber;

        if (userRole.equals("FRANCHISE")) {
            orderUserId = userId;
            // franchiseId
            franchiseId = userManagementService.getFranchiseIdByUserId(userId);
            // username
            username = userManagementService.getUsernameByUserId(userId);
            // phoneNumber
            phoneNumber = userManagementService.getPhoneNumberByUserId(userId);
        } else if (userRole.equals("HQ")) {
            FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByHQ(orderCode);
            orderUserId = order.userId();

            // franchiseId
            franchiseId = userManagementService.getFranchiseIdByUserId(orderUserId);
            // username
            username = userManagementService.getUsernameByUserId(orderUserId);
            // phoneNumber
            phoneNumber = userManagementService.getPhoneNumberByUserId(orderUserId);
        } else {
            throw new OrderException(OrderErrorCode.UNAUTHORIZED);
        }

        // FranchiseOrderCommand
        FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, orderUserId, orderCode);

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

        // Map<orderItemId, FranchiseOrderItemCommand>>
        Map<Long, FranchiseOrderItemCommand> orderItemCommandByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // List<FranchiseOrderItemCommand>
        List<FranchiseOrderItemCommand> orderItems = orderItemsByOrderId.values().stream().flatMap(List::stream).toList();

        // List<productId>
        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(FranchiseOrderItemCommand::productId)
                .toList();

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = franchiseOrderService.getOrderItemIdsAndProductIdsByOrderId(order.orderId());

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // List<FranchiseOrderItemDetailResponse>
        List<FranchiseOrderItemDetailResponse> itemResponses = orderItemIdsByProductId.entrySet().stream()
                .flatMap(entry -> {
                    List<Long> orderItemIds = entry.getValue();

                    return orderItemIds.stream()
                            .map(orderItemId -> {
                                FranchiseOrderItemCommand orderItem = orderItemCommandByOrderItemId.get(orderItemId);
                                ProductInfo productInfo = productInfoByProductId.get(entry.getKey());

                                if (productInfo == null) {
                                    throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                                }

                                return FranchiseOrderItemDetailResponse.builder()
                                        .productCode(productInfo.productCode())
                                        .productName(productInfo.productName())
                                        .quantity(orderItem.quantity())
                                        .unitPrice(orderItem.unitPrice())
                                        .totalPrice(orderItem.unitPrice().multiply(BigDecimal.valueOf(orderItem.quantity())))
                                        .build();
                            });
                })
                .toList();

        // 반환
        FranchiseOrderDetailResponse result = FranchiseOrderDetailResponse.builder()
                .orderCode(order.orderCode())
                .status(order.orderStatus())
                .requestedDate(order.requestedDate())
                .receiver(username)
                .phoneNumber(phoneNumber)
                .address(order.address())
                .deliveryDate(order.deliveryDate())
                .deliveryTime(order.deliveryTime())
                .items(itemResponses)
                .build();
        writeCache(cacheKey, result);
        return result;
    }

    // 가맹점의 발주 수정
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderUpdateResponse updateOrder(Long userId, String orderCode, List<FranchiseOrderUpdateRequest> requests) {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(STOCK_LOCK_KEY, lockValue, STOCK_LOCK_TTL);

        if (!Boolean.TRUE.equals(locked)) {
            throw new OrderException(OrderErrorCode.ORDER_CONFLICT);
        }

        try {
            // List<FranchiseOrderCodeAndQuantityCommand>
            List<FranchiseOrderCodeAndQuantityCommand> requestItemCommands = requests.stream().map(FranchiseOrderCodeAndQuantityCommand::from).toList();

            // Set<productCode>
            Set<String> productCodes = requests.stream().map(FranchiseOrderUpdateRequest::productCode).collect(Collectors.toSet());

            // Map<productCode, ProductInfo>
            Map<String, ProductInfo> productInfoByProductCode = productService.getProductInfosByProductCode(productCodes);

            // 재고 체크
            inventoryService.checkStock(requestItemCommands, productInfoByProductCode);

            // franchiseId
            Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

            // FranchiseOrderDetailCommand
            FranchiseOrderDetailCommand order = franchiseOrderService.getOrderByOrderCode(franchiseId, userId, orderCode);

            // Map<orderId, List<FranchiseOrderItemCommand>>
            Map<Long, List<FranchiseOrderItemCommand>> orderItemsByOrderId = franchiseOrderService.getOrderItemsByOrderId(order.orderId());

            // List<productId>
            List<Long> productIds = orderItemsByOrderId.values().stream()
                    .flatMap(List::stream)
                    .map(FranchiseOrderItemCommand::productId)
                    .toList();

            // Map<productId, ProductInfo>
            Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

            // Map<productId, FranchiseOrderUpdateRequest>
            Map<Long, FranchiseOrderUpdateRequest> requestByProductId = requests.stream()
                    .collect(Collectors.toMap(
                            request -> productInfoByProductCode.get(request.productCode()).productId(),
                            Function.identity()
                    ));

            // 발주 수정
            List<FranchiseOrderItemDetailResponse> itemResponses = franchiseOrderService.updateOrder(order.orderId(), requestByProductId, productInfoByProductId);

            FranchiseOrderUpdateResponse result = FranchiseOrderUpdateResponse.builder()
                    .orderCode(orderCode)
                    .cancelReason(order.canceledReason())
                    .items(itemResponses)
                    .build();
            evictByPattern("ord:fr:*");
            evictByPattern("ord:hq:*");
            return result;
        } finally {
            String current = redisTemplate.opsForValue().get(STOCK_LOCK_KEY);
            if (lockValue.equals(current)) {
                redisTemplate.delete(STOCK_LOCK_KEY);
            }
        }
    }

    // 가맹점 발주 취소
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderCancelResponse cancelOrder(Long userId, String orderCode) {
        // franchiseId
        Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

        // 취소
        FranchiseOrderCancelResponse result = franchiseOrderService.cancelOrder(userId, franchiseId, orderCode);
        evictByPattern("ord:fr:*");
        evictByPattern("ord:hq:*");
        return result;
    }

    // 가맹점 발주 생성
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public FranchiseOrderCreateResponse createOrder(Long userId, FranchiseOrderCreateRequest request) {
        long startTime = System.currentTimeMillis();

        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(STOCK_LOCK_KEY, lockValue, STOCK_LOCK_TTL);

        if (!Boolean.TRUE.equals(locked)) {
            throw new OrderException(OrderErrorCode.ORDER_CONFLICT);
        }

        try {
            // Set<productCode>
            Set<String> productCodes = request.items().stream().map(FranchiseOrderCreateRequestItem::productCode).collect(Collectors.toSet());

            // Map<productCode, ProductInfo>
            Map<String, ProductInfo> productInfoByProductCode = productService.getProductInfosByProductCode(productCodes);

            // List<FranchiseOrderCodeAndQuantityCommand>
            List<FranchiseOrderCodeAndQuantityCommand> requestItemCommands = request.items().stream().map(FranchiseOrderCodeAndQuantityCommand::from).toList();

            // 발주 가능한지 재고 확인
            inventoryService.checkStock(requestItemCommands, productInfoByProductCode);

            // franchiseId
            Long franchiseId = userManagementService.getFranchiseIdByUserId(userId);

            // franchiseCode
            String franchiseCode = franchiseService.getById(franchiseId).businessNumber();
            log.info("franchiseId: {}", franchiseId);
            log.info("userId: {}", userId);

            // username
            String username = userManagementService.getUsernameByUserId(userId);

            // orderCode
            String orderCode = generator.generate(franchiseCode);

            // FranchiseOrderCommand
            FranchiseOrderCommand order = franchiseOrderService.createOrder(request, orderCode, franchiseId, userId, productInfoByProductCode);

            // List<FranchiseOrderItemCommand>
            List<FranchiseOrderItemDetailResponse> orderItems = franchiseOrderService.createOrderItems(request, productInfoByProductCode, orderCode);

            // 필요 값
            BigDecimal totalPrice = orderItems.stream()
                    .map(FranchiseOrderItemDetailResponse::totalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 반환
            FranchiseOrderCreateResponse result = FranchiseOrderCreateResponse.builder()
                    .orderCode(orderCode)
                    .orderStatus(order.orderStatus())
                    .totalPrice(totalPrice)
                    .requestedDate(order.requestedDate())
                    .receiver(username)
                    .deliveryDate(order.deliveryDate())
                    .items(orderItems)
                    .build();
            evictByPattern("ord:fr:*");
            evictByPattern("ord:hq:*");
            return result;
        } finally {
            String current = redisTemplate.opsForValue().get(STOCK_LOCK_KEY);
            if (lockValue.equals(current)) {
                redisTemplate.delete(STOCK_LOCK_KEY);
            }
            log.info("[발주 생성] userId={}, 처리 시간={}ms", userId, System.currentTimeMillis() - startTime);
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