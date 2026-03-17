package com.chaing.api.facade.factory;

import com.chaing.api.config.RedisCacheHelper;
import com.chaing.core.dto.command.FranchiseOrderCodeAndQuantityCommand;
import com.chaing.domain.businessunits.service.impl.FranchiseServiceImpl;
import com.chaing.domain.businessunits.service.impl.HeadquarterServiceImpl;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.HQOrderCancelCommand;
import com.chaing.domain.orders.dto.response.HQOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQRequestedOrderItemProjection;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderStatusUpdateRequest;
import com.chaing.domain.orders.dto.request.HQFranchiseOrderCancelRequest;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateCommand;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderStatusShippingPendingResponse;
import com.chaing.domain.orders.dto.response.HQFranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCreateResponse;
import com.chaing.domain.orders.dto.response.HQOrderDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.dto.response.HQOrderUpdateResponse;
import com.chaing.domain.orders.dto.response.HQRequestedOrderResponse;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HQOrderFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final HQOrderService hqOrderService;
    private final FranchiseOrderService franchiseOrderService;
    private final ProductService productService;
    private final UserManagementService userManagementService;
    private final FranchiseServiceImpl franchiseService;
    private final HeadquarterServiceImpl headquarterService;
    private final InventoryService inventoryService;
    private final RedisCacheHelper redisCacheHelper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 발주 조회
    public List<HQOrderResponse> getAllOrders() {
        String cacheKey = "ord:hq:all";
        List<HQOrderResponse> cached = readListCache(cacheKey, new TypeReference<>() {});
        if (cached != null) return cached;

        // 발주 정보 조회
        // Map<orderId, HQOrderCommand>
        Map<Long, HQOrderCommand> orders = hqOrderService.getAllOrders();

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        // Map<userId, username>
        Map<Long, String> usernameByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        HQOrderCommand::userId,
                        command -> userManagementService.getUsernameByUserId(command.userId()),
                        (a , b) -> a
                ));

        // Map<userId, phoneNumber>
        Map<Long, String> phoneNumberByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        HQOrderCommand::userId,
                        command -> userManagementService.getPhoneNumberByUserId(command.userId()),
                        (a , b) -> a
                ));

        // 발주 제품 정보 조회
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<com.chaing.domain.orders.dto.command.HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemIdsByOrderId(orderIds);

        // Map<orderItemId, HQOrderItemCommand>
        Map<Long, com.chaing.domain.orders.dto.command.HQOrderItemCommand> orderItemByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId,
                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::productId
                ));

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream()
                .distinct()
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<orderId, Map<productId, List<orderItemId>>>
        Map<Long, Map<Long, List<Long>>> productIdOrderItemIdByOrderId = orderItemsByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        com.chaing.domain.orders.dto.command.HQOrderItemCommand::productId,
                                        Collectors.mapping(com.chaing.domain.orders.dto.command.HQOrderItemCommand::orderItemId, Collectors.toList())
                                ))
                ));

        // 반환
        List<HQOrderResponse> result = productIdOrderItemIdByOrderId.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    HQOrderCommand order = orders.get(orderId);
                    Map<Long, List<Long>> orderItemByProductId = entry.getValue();

                    return orderItemByProductId.entrySet().stream()
                            .map(entrySet -> {
                                Long productId = entrySet.getKey();
                                List<Long> orderItemIds = entrySet.getValue();
                                Long userId = order.userId();
                                String username = usernameByUserId.get(userId);
                                String phoneNumber = phoneNumberByUserId.get(userId);
                                List<com.chaing.domain.orders.dto.command.HQOrderItemCommand> orderItems = orderItemIds.stream()
                                        .map(orderItemByOrderItemId::get)
                                        .toList();
                                Integer quantity = orderItems.stream()
                                        .map(com.chaing.domain.orders.dto.command.HQOrderItemCommand::quantity)
                                        .reduce(0, Integer::sum);
                                ProductInfo productInfo = productInfoByProductId.get(productId);

                                if (productInfo == null) {
                                    throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                                }

                                return HQOrderResponse.builder()
                                        .orderCode(order.orderCode())
                                        .status(order.status())
                                        .quantity(quantity)
                                        .username(username)
                                        .phoneNumber(phoneNumber)
                                        .requestedDate(order.requestedDate())
                                        .manufacturedDate(order.manufacturedDate())
                                        .storedDate(order.storedDate())
                                        .productCode(productInfo.productCode())
                                        .build();
                            });
                })
                .toList();
        writeCache(cacheKey, result);
        return result;
    }

    // 발주 페이지네이션 조회 (아이템 행 단위)
    public Page<HQOrderResponse> getAllOrdersPaged(Pageable pageable) {
        String cacheKey = "ord:hq:page:%s".formatted(pageableKey(pageable));
        Page<HQOrderResponse> cached = readPageCache(cacheKey, HQOrderResponse.class, pageable);
        if (cached != null) return cached;

        Page<HQOrderItemProjection> page = hqOrderService.getOrderItemPage(pageable);

        if (page.isEmpty()) {
            Page<HQOrderResponse> empty = new PageImpl<>(List.of(), pageable, 0);
            writePageCache(cacheKey, empty);
            return empty;
        }

        // userId → username, phoneNumber 매핑
        List<Long> userIds = page.getContent().stream()
                .map(HQOrderItemProjection::userId).distinct().toList();
        Map<Long, String> usernameByUserId = userIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                        userManagementService::getUsernameByUserId));
        Map<Long, String> phoneNumberByUserId = userIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                        userManagementService::getPhoneNumberByUserId));

        // productId → productCode 매핑
        List<Long> productIds = page.getContent().stream()
                .map(HQOrderItemProjection::productId).distinct().toList();
        Map<Long, ProductInfo> productInfoMap = productService.getProductInfos(productIds);

        List<HQOrderResponse> content = page.getContent().stream()
                .map(p -> HQOrderResponse.builder()
                        .orderCode(p.orderCode())
                        .status(p.status())
                        .quantity(p.quantity())
                        .username(usernameByUserId.get(p.userId()))
                        .phoneNumber(phoneNumberByUserId.get(p.userId()))
                        .requestedDate(p.requestedDate())
                        .manufacturedDate(p.manufacturedDate())
                        .storedDate(p.storedDate())
                        .productCode(productInfoMap.containsKey(p.productId())
                                ? productInfoMap.get(p.productId()).productCode() : null)
                        .build())
                .toList();

        Page<HQOrderResponse> result = new PageImpl<>(content, pageable, page.getTotalElements());
        writePageCache(cacheKey, result);
        return result;
    }

    // 특정 발주 조회
    public HQOrderDetailResponse getOrderDetail(String orderCode) {
        String cacheKey = "ord:hq:detail:%s".formatted(orderCode);
        HQOrderDetailResponse cached = readObjectCache(cacheKey, HQOrderDetailResponse.class);
        if (cached != null) return cached;

        // 발주 정보 조회
        HQOrderCommand order = hqOrderService.getOrder(orderCode);

        // UserInfo
        Long userId = order.userId();
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemsByOrderId(order.orderId());

        // Map<orderItemId, HQOrderItemCommand>
        Map<Long, HQOrderItemCommand> orderItemByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        HQOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // List<productId>
        List<Long> productIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(HQOrderItemCommand::productId)
                .toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdByProductId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                        HQOrderItemCommand::productId,
                        Collectors.mapping(HQOrderItemCommand::orderItemId, Collectors.toList())
                ));

        // List<HQOrderItemCommand> — productCode 보강
        List<HQOrderItemCommand> items = orderItemByOrderItemId.values().stream()
                .map(item -> {
                    ProductInfo info = productInfoByProductId.get(item.productId());

                    if (info == null) {
                        throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return HQOrderItemCommand.builder()
                            .orderId(item.orderId())
                            .orderItemId(item.orderItemId())
                            .productId(item.productId())
                            .productCode(info.productCode())
                            .quantity(item.quantity())
                            .unitPrice(item.unitPrice())
                            .totalPrice(item.totalPrice())
                            .build();
                })
                .toList();

        // 반환
        HQOrderDetailResponse result = HQOrderDetailResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .manufacturedDate(order.manufacturedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
        writeCache(cacheKey, result);
        return result;
    }

    // 발주 수정
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderUpdateResponse updateOrder(Long userId, String orderCode, HQOrderUpdateRequest request) {
        // UserInfo
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        // HQOrderCommand
        HQOrderCommand order = hqOrderService.getOrderByUserIdAndOrderCodeAndPending(userId, orderCode);

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));
        log.info("productInfoByProductCode: {}", productInfoByProductCode);
        // 발주 제품 데이터 수정
        List<HQOrderItemCommand> items = hqOrderService.updateOrderItems(userId, orderCode, request, productInfoByProductCode);

        // 반환
        HQOrderUpdateResponse result = HQOrderUpdateResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .manufacturedDate(order.manufacturedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
    }

    // 발주 취소
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCancelResponse cancel(Long userId, String orderCode) {
        // 취소
        HQOrderCancelCommand cancelOrder = hqOrderService.cancel(userId, orderCode);

        // 반환
        HQOrderCancelResponse result = HQOrderCancelResponse.from(cancelOrder);
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
    }

    // 가맹점 발주 상태 변경(접수/반려)
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQOrderStatusUpdateResponse> updateStatus(HQOrderUpdateStatusRequest request) {
        // 접수 시에만 재고 확인
        if (request.isAccepted()) {
            // Map<orderId, FranchiseOrderDetailCommand>
            Map<Long, FranchiseOrderDetailCommand> orderByOrderId = franchiseOrderService.getOrdersByOrderCode(request.orderCodes());

            // List<orderId>
            List<Long> orderIds = orderByOrderId.values().stream().map(FranchiseOrderDetailCommand::orderId).collect(Collectors.toList());

            // Map<orderId, List<FranchiseOrderItemCommand>>
            Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderItemId = franchiseOrderService.getOrderItemsByOrderIds(orderIds);

            // Map<productId, ProductInfo>
            Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

            // List<FranchiseOrderCodeAndQuantityCommand> requestItemCommands
            List<FranchiseOrderCodeAndQuantityCommand> requestItemCommands = orderItemByOrderItemId.values().stream()
                    .flatMap(List::stream)
                    .map(item -> {
                        Long productId = item.productId();
                        ProductInfo productInfo = productInfoByProductId.get(productId);

                        if (productInfo == null) {
                            throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                        }

                        return FranchiseOrderCodeAndQuantityCommand.builder()
                                .productCode(productInfo.productCode())
                                .quantity(item.quantity())
                                .build();
                    })
                    .toList();

            // Map<orderCode, ProductInfo> productInfoByProductCode
            Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                    .collect(Collectors.toMap(
                            ProductInfo::productCode,
                            Function.identity()
                    ));

            // 발주 가능한지 재고 확인
            inventoryService.checkStock(requestItemCommands, productInfoByProductCode);
        }

        // 상태 변경 및 반환
        List<HQOrderStatusUpdateResponse> result = franchiseOrderService.updateStatus(request);
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
    }

    // 발주 생성
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public HQOrderCreateResponse create(Long userId, HQOrderCreateRequest request) {
        // HQCode
        Long hqId = userManagementService.getBusinessUnitIdByUserId(userId);
        String hqCode = headquarterService.getHqCode(hqId);

        // UserInfo
        String username = userManagementService.getUsernameByUserId(userId);
        String phoneNumber = userManagementService.getPhoneNumberByUserId(userId);

        if (!username.equals(request.username()) || !phoneNumber.equals(request.phoneNumber())) {
            throw new HQOrderException(HQOrderErrorCode.INVALID_USER_INFO);
        }

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getAllProductInfo();

        // 발주 생성
        HQOrderCommand order = hqOrderService.createOrder(userId, request, hqCode, productInfoByProductId);

        // 발주 제품 생성 - 수정요망
        List<HQOrderItemCommand> items = hqOrderService.createOrderItems(order.orderId(), productInfoByProductId, request.items());

        // 반환
        HQOrderCreateResponse result = HQOrderCreateResponse.builder()
                .orderCode(order.orderCode())
                .status(order.status())
                .username(username)
                .phoneNumber(phoneNumber)
                .requestedDate(order.requestedDate())
                .storedDate(order.storedDate())
                .description(order.description())
                .isRegular(order.isRegular())
                .items(items)
                .build();
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
    }

    // 가맹점 발주 요청 페이지네이션 조회
    public Page<HQRequestedOrderResponse> getRequestedOrdersPaged(boolean isPending, Pageable pageable) {
        String cacheKey = "ord:hq:requested:page:%s:%s".formatted(isPending, pageableKey(pageable));
        Page<HQRequestedOrderResponse> cached = readPageCache(cacheKey, HQRequestedOrderResponse.class, pageable);
        if (cached != null) return cached;

        Page<HQRequestedOrderItemProjection> page = franchiseOrderService.getRequestedOrderItemPage(isPending, pageable);

        if (page.isEmpty()) {
            Page<HQRequestedOrderResponse> empty = new PageImpl<>(List.of(), pageable, 0);
            writePageCache(cacheKey, empty);
            return empty;
        }

        // userId → username 매핑
        List<Long> userIds = page.getContent().stream()
                .map(HQRequestedOrderItemProjection::userId).distinct().toList();
        Map<Long, String> usernameByUserId = userIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                        userManagementService::getUsernameByUserId));

        // userId → franchiseCode 매핑
        Map<Long, String> franchiseCodeByUserId = userIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                        uid -> franchiseService.getById(userManagementService.getFranchiseIdByUserId(uid)).code()));

        // productId → productCode 매핑
        List<Long> productIds = page.getContent().stream()
                .map(HQRequestedOrderItemProjection::productId).distinct().toList();
        Map<Long, ProductInfo> productInfoMap = productService.getProductInfos(productIds);

        List<HQRequestedOrderResponse> content = page.getContent().stream()
                .map(p -> HQRequestedOrderResponse.builder()
                        .orderCode(p.orderCode())
                        .franchiseCode(franchiseCodeByUserId.get(p.userId()))
                        .receiver(usernameByUserId.get(p.userId()))
                        .productCode(productInfoMap.containsKey(p.productId())
                                ? productInfoMap.get(p.productId()).productCode() : null)
                        .status(p.orderStatus())
                        .quantity(p.quantity())
                        .deliveryDate(p.deliveryDate())
                        .build())
                .toList();

        Page<HQRequestedOrderResponse> result = new PageImpl<>(content, pageable, page.getTotalElements());
        writePageCache(cacheKey, result);
        return result;
    }

    // 가맹점 발주 요청 조회 (기존)
    public List<HQRequestedOrderResponse> getRequestedOrders(boolean isPending) {
        String cacheKey = "ord:hq:requested:%s".formatted(isPending);
        List<HQRequestedOrderResponse> cached = readListCache(cacheKey, new TypeReference<>() {});
        if (cached != null) return cached;

        // Map<orderId, FranchiseOrderDetail>
        Map<Long, FranchiseOrderDetailCommand> orders;
        if (isPending) {
            // 대기 상태 요청 조회
            orders = franchiseOrderService.getAllRequestedOrders();
        } else {
            // 전체 요청 조회
            orders = franchiseOrderService.getAllOrders();
        }

        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        // List<orderId>
        List<Long> orderIds = orders.keySet().stream().toList();

        // Map<orderId, List<FranchiseOrderItemCommand>>
        Map<Long, List<FranchiseOrderItemCommand>> orderItemByOrderId = franchiseOrderService.getAllRequestedOrderItem(orderIds);

        // Map<userId, username>
        Map<Long, String> usernameByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::userId,
                        order -> userManagementService.getUsernameByUserId(order.userId()),
                        (a, b) -> a
                ));

        // Map<userId, franchiseCode>
        Map<Long, String> franchiseCodeByUserId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::userId,
                        order -> {
                            try {
                                return franchiseService.getById(userManagementService.getFranchiseIdByUserId(order.userId())).code();
                            } catch (Exception e) {
                                throw new HQOrderException(HQOrderErrorCode.INVALID_USER_INFO);
                            }
                        },
                        (a, b) -> a
                ));

        // Map<orderItemId, FranchiseOrderItemCommand>
        Map<Long, FranchiseOrderItemCommand> orderItemByOrderItemId = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // Map<orderId, receiver>
        Map<Long, String> receiverByOrderId = orders.values().stream()
                .collect(Collectors.toMap(
                        FranchiseOrderDetailCommand::orderId,
                        command -> userManagementService.getUsernameByUserId(command.userId())
                ));

        // Map<orderId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByOrderId = orderItemByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(FranchiseOrderItemCommand::orderItemId)
                                .toList()
                ));

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = orderItemByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        FranchiseOrderItemCommand::orderItemId,
                        FranchiseOrderItemCommand::productId
                ));

        // Map<orderId, Map<productId, List<orderItemId>>>
        Map<Long, Map<Long, List<Long>>> productIdOrderItemIdByOrderId = orderItemByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        FranchiseOrderItemCommand::productId,
                                        Collectors.mapping(FranchiseOrderItemCommand::orderItemId, Collectors.toList())
                                ))
                ));

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // 반환
        List<HQRequestedOrderResponse> result = productIdOrderItemIdByOrderId.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();
                    FranchiseOrderDetailCommand order = orders.get(orderId);
                    Map<Long, List<Long>> orderItemIdsByProductId = entry.getValue();

                    return orderItemIdsByProductId.entrySet().stream()
                            .map(entrySet -> {
                                Long productId = entrySet.getKey();
                                List<Long> orderItemIds = entrySet.getValue();
                                Long orderUserId = order.userId();
                                String username = usernameByUserId.get(orderUserId);
                                String franchiseCode = franchiseCodeByUserId.get(orderUserId);

                                List<FranchiseOrderItemCommand> orderItems = orderItemIds.stream()
                                        .map(orderItemByOrderItemId::get)
                                        .toList();
                                ProductInfo productInfo = productInfoByProductId.get(productId);
                                if (productInfo == null) {
                                    throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                                }

                                Integer quantity = orderItems.stream()
                                        .map(FranchiseOrderItemCommand::quantity)
                                        .reduce(0, Integer::sum);

                                return HQRequestedOrderResponse.builder()
                                        .orderCode(order.orderCode())
                                        .franchiseCode(franchiseCode)
                                        .receiver(username)
                                        .productCode(productInfo.productCode())
                                        .status(order.orderStatus())
                                        .quantity(quantity)
                                        .deliveryDate(order.deliveryDate())
                                        .build();
                            });
                })
                .toList();
        writeCache(cacheKey, result);
        return result;
    }

    // 발주 상태 SHIPPING_PENDING으로 수정
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<FranchiseOrderStatusShippingPendingResponse> updateShippingPending(List<FranchiseOrderStatusUpdateRequest> requests) {
        // List<orderCode>
        Set<String> orderCodes = requests.stream().map(FranchiseOrderStatusUpdateRequest::orderCode).collect(Collectors.toSet());

        // 수정
        // Map<orderId, FranchiseOrderCommand>
        Map<Long, FranchiseOrderCommand> orders = franchiseOrderService.updateShippingPending(orderCodes);

        // 반환
        List<FranchiseOrderStatusShippingPendingResponse> result = orders.values().stream()
                .map(FranchiseOrderStatusShippingPendingResponse::from)
                .toList();
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
    }

    // 가맹점의 발주 요청 취소
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<HQFranchiseOrderCancelResponse> cancelFranchiseOrder(@Valid List<HQFranchiseOrderCancelRequest> request) {
        // 수정
        // Map<orderCode, FranchiseOrderStatus>
        Map<String, FranchiseOrderStatus> statusByOrderCode = franchiseOrderService.cancelFranchiseOrder(request);

        // 반환
        List<HQFranchiseOrderCancelResponse> result = statusByOrderCode.entrySet().stream()
                .map(HQFranchiseOrderCancelResponse::of)
                .toList();
        redisCacheHelper.evictByPattern("ord:hq:*");
        redisCacheHelper.evictByPattern("ord:fr:*");
        return result;
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
