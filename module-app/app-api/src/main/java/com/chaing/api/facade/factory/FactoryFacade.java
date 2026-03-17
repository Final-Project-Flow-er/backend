package com.chaing.api.facade.factory;

import com.chaing.api.config.RedisCacheHelper;
import com.chaing.core.dto.command.UserContactCommand;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.command.HQOrderItemCommand;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.request.FactoryOrderRequest;
import com.chaing.domain.orders.dto.response.FactoryOrderItemProjection;
import com.chaing.domain.orders.dto.response.FactoryOrderResponse;
import com.chaing.domain.orders.dto.response.FactoryOrderUpdateResponse;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryFacade {

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final HQOrderService hqOrderService;
    private final ProductService productService;
    private final UserManagementService userManagementService;
    private final RedisCacheHelper redisCacheHelper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 발주 전체/대기 조회 (페이지네이션)
    public Page<FactoryOrderResponse> getAllOrdersPaged(boolean isAll, Pageable pageable) {
        String cacheKey = "ord:fc:page:%s:%s".formatted(isAll, pageableKey(pageable));
        Page<FactoryOrderResponse> cached = readPageCache(cacheKey, FactoryOrderResponse.class, pageable);
        if (cached != null) return cached;

        Page<FactoryOrderItemProjection> page = hqOrderService.getFactoryOrderItemPage(isAll, pageable);

        if (page.isEmpty()) {
            Page<FactoryOrderResponse> empty = new PageImpl<>(List.of(), pageable, 0);
            writePageCache(cacheKey, empty);
            return empty;
        }

        // userId → username, phoneNumber
        List<Long> userIds = page.getContent().stream()
                .map(FactoryOrderItemProjection::userId).distinct().toList();
        Map<Long, UserContactCommand> userByUserId = userManagementService.getUserContactInfosByUserIds(userIds);

        // productId → ProductInfo
        List<Long> productIds = page.getContent().stream()
                .map(FactoryOrderItemProjection::productId).distinct().toList();
        Map<Long, ProductInfo> productInfoMap = productService.getProductInfos(productIds);

        List<FactoryOrderResponse> content = page.getContent().stream()
                .map(p -> {
                    UserContactCommand user = userByUserId.get(p.userId());
                    ProductInfo productInfo = productInfoMap.get(p.productId());
                    return FactoryOrderResponse.builder()
                            .orderCode(p.orderCode())
                            .status(p.status())
                            .isRegular(p.isRegular())
                            .productCode(productInfo != null ? productInfo.productCode() : null)
                            .productName(productInfo != null ? productInfo.productName() : null)
                            .quantity(p.quantity())
                            .username(user != null ? user.username() : null)
                            .phoneNumber(user != null ? user.phoneNumber() : null)
                            .requestedDate(p.requestedDate())
                            .storedDate(p.storedDate())
                            .build();
                })
                .toList();

        Page<FactoryOrderResponse> result = new PageImpl<>(content, pageable, page.getTotalElements());
        writePageCache(cacheKey, result);
        return result;
    }

    // 발주 전체/대기 조회 (기존)
    public List<FactoryOrderResponse> getAllOrders(boolean isAll) {
        // Map<orderId, HQOrderCommand>
        Map<Long, HQOrderCommand> ordersByOrderId;

        if (isAll) {
            // 전체 발주 조회
            ordersByOrderId = hqOrderService.getAllOrdersByFactory();
        } else {
            // 대기 발주 조회
            ordersByOrderId = hqOrderService.getAllPendingOrders();
        }
        log.info("ordersByOrderId = {}", ordersByOrderId);
        // 발주 존재하지 않을 시 빈 배열 반환
        if (ordersByOrderId == null || ordersByOrderId.isEmpty()) {
            return List.of();
        }

        // List<orderId>
        List<Long> orderIds = ordersByOrderId.keySet().stream().toList();

        // List<userId>
        List<Long> userIds = ordersByOrderId.values().stream()
                .map(HQOrderCommand::userId)
                .distinct()
                .toList();

        // Map<userId, UserContactCommand>
        Map<Long, UserContactCommand> userByUserId = userManagementService.getUserContactInfosByUserIds(userIds);
        log.info("userByUserId = {}", userByUserId);
        if (userByUserId == null || userByUserId.isEmpty()) {
            return List.of();
        }

        // Map<orderId, userId>
        Map<Long, Long> userIdByOrderId = ordersByOrderId.values().stream()
                .collect(Collectors.toMap(
                        HQOrderCommand::orderId,
                        HQOrderCommand::userId
                ));

        // Map<orderId, username>
        Map<Long, String> usernameByOrderId = userIdByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userByUserId.get(entry.getValue()).username()
                ));

        // Map<orderId, phoneNumber>
        Map<Long, String> phoneNumberByOrderId = userIdByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userByUserId.get(entry.getValue()).phoneNumber()
                ));

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemIdsByOrderId(orderIds);
        log.info("orderItemsByOrderId = {}", orderItemsByOrderId);
        // Map<orderItemId, HQOrderItemCommand>
        Map<Long, HQOrderItemCommand> orderItemByOrderItemId = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        HQOrderItemCommand::orderItemId,
                        Function.identity()
                ));

        // List<orderItemId>
        List<Long> orderItemIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(HQOrderItemCommand::orderItemId)
                .toList();

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = hqOrderService.getProductIdsByOrderItemIds(orderItemIds);

        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().distinct().toList();

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);

        // Map<orderId, Map<productId, List<orderItemId>>
        Map<Long, Map<Long, List<Long>>> orderItemIdsByProductIdByOrderId = orderItemsByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        item -> productIdByOrderItemId.get(item.orderItemId()),
                                        Collectors.mapping(HQOrderItemCommand::orderItemId, Collectors.toList())
                                ))
                ));

        // 반환
        return orderItemIdsByProductIdByOrderId.entrySet().stream()
                .flatMap(entry -> {
                    Long orderId = entry.getKey();

                    HQOrderCommand order = ordersByOrderId.get(orderId);
                    Map<Long, List<Long>> orderItemIdsByProductId = entry.getValue();

                    String username = usernameByOrderId.get(orderId);
                    String phoneNumber = phoneNumberByOrderId.get(orderId);

                    return orderItemIdsByProductId.entrySet().stream()
                            .map(entrySet -> {
                                Long productId = entrySet.getKey();
                                Integer quantity = entrySet.getValue().stream()
                                        .map(orderItemId -> orderItemByOrderItemId.get(orderItemId).quantity())
                                        .reduce(0, Integer::sum);

                                ProductInfo productInfo = productInfoByProductId.get(productId);

                                return FactoryOrderResponse.builder()
                                        .orderCode(order.orderCode())
                                        .status(order.status())
                                        .isRegular(order.isRegular())
                                        .productCode(productInfo.productCode())
                                        .productName(productInfo.productName())
                                        .quantity(quantity)
                                        .username(username)
                                        .phoneNumber(phoneNumber)
                                        .requestedDate(order.requestedDate())
                                        .storedDate(order.storedDate())
                                        .build();
                                    }
                            );
                })
                .toList();
    }

    // 발주 접수/반려
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<FactoryOrderUpdateResponse> updateOrders(FactoryOrderRequest request, boolean isAccept) {
        // Map<orderCode, HQOrderStatus>
        Map<String, HQOrderStatus> orderStatusByOrderCode = hqOrderService.updateOrders(request.orderCodes(), isAccept);

        // 반환
        List<FactoryOrderUpdateResponse> result = orderStatusByOrderCode.entrySet().stream()
                .map(entry -> FactoryOrderUpdateResponse.builder()
                        .orderCode(entry.getKey())
                        .status(entry.getValue())
                        .build())
                .toList();
        redisCacheHelper.evictByPattern("ord:fc:*");
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
