package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.core.enums.LogType;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.core.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.core.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQFranchiseOrderCancelRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import com.chaing.domain.orders.dto.response.FranchiseOrderItemProjection;
import com.chaing.domain.orders.dto.response.HQRequestedOrderItemProjection;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FranchiseOrderService {

    private final FranchiseOrderRepository franchiseOrderRepository;
    private final FranchiseOrderItemRepository franchiseOrderItemRepository;

    // 가맹점 발주 목록 조회
    public Map<Long, FranchiseOrderCommand> getAllOrdersByFranchiseIdAndUserId(Long franchiseId, Long userId) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseIdAndUserId(franchiseId, userId);

        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId, FranchiseOrderCommand::from
                ));
    }

    // 가맹점 발주 목록 페이지네이션 조회 (아이템 행 단위)
    public Page<FranchiseOrderItemProjection> getOrderItemPage(
            Long franchiseId, Long userId, Pageable pageable) {
        return franchiseOrderRepository.findOrderItemPage(franchiseId, userId, pageable);
    }

    // 본사용 가맹점 발주 요청 페이지네이션 조회 (아이템 행 단위)
    public Page<HQRequestedOrderItemProjection> getRequestedOrderItemPage(boolean isPending, Pageable pageable) {
        return franchiseOrderRepository.findRequestedOrderItemPage(isPending, pageable);
    }

    // 발주 번호에 따른 가맹점 특정 발주 조회
    public FranchiseOrderDetailCommand getOrderByOrderCode(Long franchiseId, Long userId, String orderCode) {
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return FranchiseOrderDetailCommand.from(order);
    }

    // 가맹점의 발주 수정
    public List<FranchiseOrderItemDetailResponse> updateOrder(Long orderId, Map<Long, FranchiseOrderUpdateRequest> requestByProductId, Map<Long, ProductInfo> productInfoByProductId) {
        // 발주 조회
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!order.getOrderStatus().equals(FranchiseOrderStatus.PENDING)) {
            throw new OrderException(OrderErrorCode.INVALID_STATUS);
        }

        // 발주 제품 조회
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(orderId);

        if (items == null || items.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // Map<productId, FranchiseOrderItem>
        Map<Long, FranchiseOrderItem> existingItem = items.stream().collect(Collectors.toMap(FranchiseOrderItem::getProductId, Function.identity()));

        // 삭제 대상
        List<FranchiseOrderItem> deletedItems = items.stream().filter(item -> !requestByProductId.containsKey(item.getProductId())).toList();

        // 수정/추가
        List<FranchiseOrderItem> upsertItems = requestByProductId.entrySet().stream().map(entry -> {
            Long productId = entry.getKey();
            FranchiseOrderUpdateRequest request = entry.getValue();
            FranchiseOrderItem item = existingItem.get(productId);
            ProductInfo productInfo = productInfoByProductId.get(productId);

            if (item != null) {
                // 수정
                item.updateQuantity(request.quantity());
                return item;
            } else {
                // 추가
                return FranchiseOrderItem.builder().franchiseOrder(order).productId(productId).quantity(request.quantity()).unitPrice(productInfo.tradePrice()).build();
            }
        }).toList();

        // 변경사항 저장
        franchiseOrderItemRepository.deleteAll(deletedItems);
        franchiseOrderItemRepository.saveAll(upsertItems);

        // 반환
        List<FranchiseOrderItemDetailResponse> responses = upsertItems.stream()
                .map(item -> {
                    ProductInfo productInfo = productInfoByProductId.get(item.getProductId());
                    String productCode = productInfo.productCode();
                    String productName = productInfo.productName();

                    return FranchiseOrderItemDetailResponse.builder()
                            .productCode(productCode)
                            .productName(productName)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build();
                }).toList();

        return responses;
    }

    // 가맹점 발주 취소
    public FranchiseOrderCancelResponse cancelOrder(Long userId, Long franchiseId, String orderCode) {
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCodeAndDeletedAtIsNull(franchiseId, userId, orderCode)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.cancel();

        return FranchiseOrderCancelResponse.from(order);
    }

    // 가맹점 발주 생성
    public FranchiseOrderCommand createOrder(FranchiseOrderCreateRequest request, String orderCode, Long franchiseId, Long userId, Map<String, ProductInfo> productInfoByProductCode) {
        // 필요 값
        Integer totalQuantity = request.items().stream()
                .map(FranchiseOrderCreateRequestItem::quantity)
                .reduce(0, Integer::sum);
        BigDecimal totalPrice = request.items().stream()
                .map(item -> productInfoByProductCode.get(item.productCode()).tradePrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        request.items().forEach(item -> {
            if (item.quantity() < 1) {
                throw new OrderException(OrderErrorCode.INVALID_QUANTITY);
            }
        });

        // 발주 생성
        FranchiseOrder order = FranchiseOrder.builder()
                .franchiseId(franchiseId)
                .orderCode(orderCode)
                .userId(userId)
                .address(request.address())
                .requirement(request.requirement())
                .totalQuantity(totalQuantity)
                .totalAmount(totalPrice)
                .deliveryDate(request.deliveryDate())
                .deliveryTime(request.deliveryTime())
                .build();

        // 발주 저장
        franchiseOrderRepository.save(order);

        return FranchiseOrderCommand.from(order);
    }

    private String generateOrderCode(Long franchiseId) {
        // 나중에 redis 도입으로 일련번호 초기화 실시해야 함
        return UUID.randomUUID().toString();
    }

    // 반품 대상이 되는 발주 반환
    public List<FranchiseReturnTargetResponse> getAllTargetOrders(Long franchiseId, Long userId, String username) {
        // 발주 조회
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseIdAndUserIdAndOrderStatus(franchiseId, userId, FranchiseOrderStatus.COMPLETED);

        List<String> orderCodes = orders.stream().map(FranchiseOrder::getOrderCode).toList();

        return orderCodes.stream().map(orderCode ->
                new FranchiseReturnTargetResponse(orderCode, username)
        ).toList();
    }

    // 본사에서 가맹점의 발주 상태 수정
    public List<HQOrderStatusUpdateResponse> updateStatus(HQOrderUpdateStatusRequest request) {
        // 발주 조회
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeInAndDeletedAtIsNull(request.orderCodes());

        if (orders == null || orders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        if (request.isAccepted()) {
            // 접수 처리
            orders.forEach(FranchiseOrder::accept);
        } else {
            // 반려 처리
            orders.forEach(FranchiseOrder::reject);
        }

        return HQOrderStatusUpdateResponse.from(orders);
    }

    // OrderId 조회
    public FranchiseOrder getOrderByOrderId(Long orderId) {
        return franchiseOrderRepository.findByFranchiseOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
    }

    public List<FranchiseOrderItem> getFranchiseOrderItemsByOrderId(Long orderId) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository
                .findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(orderId);
        if (items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        return items;
    }

    // OrderCode 조회
    public String getOrderCodeByOrderId(Long orderId) {
        return franchiseOrderRepository.findById(orderId).orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND)).getOrderCode();
    }

    // return: Map<productId, List<orderItemId>>
    public Map<Long, List<Long>> getOrderItemIdsAndProductIdsByOrderIds(List<Long> orderIds) {
        List<FranchiseOrderItem> orderItems = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream().collect(Collectors.groupingBy(FranchiseOrderItem::getProductId, Collectors.mapping(FranchiseOrderItem::getFranchiseOrderItemId, Collectors.toList())));
    }

    // return: Map<orderId, List<FranchiseOrderItemCommand>>
    public Map<Long, List<FranchiseOrderItemCommand>> getOrderItemsByOrderIds(List<Long> orderIds) {
        List<FranchiseOrderItem> orderItems = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);

        return orderItems.stream().collect(Collectors.groupingBy(item -> item.getFranchiseOrder().getFranchiseOrderId(), Collectors.mapping(FranchiseOrderItemCommand::from, Collectors.toList())));
    }

    // return: Map<orderId, List<FranchiseOrderItemCommand>>
    public Map<Long, List<FranchiseOrderItemCommand>> getOrderItemsByOrderId(Long orderId) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(orderId);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        Map<Long, List<FranchiseOrderItemCommand>> response = new HashMap<>();
        response.put(orderId, FranchiseOrderItemCommand.from(items));

        return response;
    }

    // Map<productId, List<orderItemId>>
    public Map<Long, List<Long>> getOrderItemIdsAndProductIdsByOrderId(Long orderId) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdAndDeletedAtIsNull(orderId);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream().collect(Collectors.groupingBy(FranchiseOrderItem::getProductId, Collectors.mapping(FranchiseOrderItem::getFranchiseOrderItemId, Collectors.toList())));
    }

    // return: List<FranchiseOrderItemDetailResponse>
    public List<FranchiseOrderItemDetailResponse> createOrderItems(FranchiseOrderCreateRequest request, Map<String, ProductInfo> productInfoByProductCode, String orderCode) {
        // 발주 조회
        FranchiseOrder order = franchiseOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productInfoByProductCode.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productId,
                        Function.identity()
                ));

        // 발주 제품 생성
        List<FranchiseOrderItem> items = request.items().stream()
                .map(item -> {
                    ProductInfo productInfo = productInfoByProductCode.get(item.productCode());

                    if (productInfo == null) {
                        throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                    }

                    Integer quantity = item.quantity();
                    Long productId = productInfo.productId();
                    BigDecimal unitPrice = productInfo.tradePrice();
                    BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));

                    return FranchiseOrderItem.builder()
                            .franchiseOrder(order)
                            .quantity(item.quantity())
                            .productId(productId)
                            .unitPrice(unitPrice)
                            .totalPrice(totalPrice)
                            .build();
                })
                .toList();

        // 발주 제품 저장
        franchiseOrderItemRepository.saveAll(items);

        // 반환
        return items.stream()
                .map(item -> {
                    ProductInfo productInfo = productInfoByProductId.get(item.getProductId());

                    if (productInfo == null) {
                        throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return FranchiseOrderItemDetailResponse.builder()
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build();
                })
                .toList();
    }

    // return: Map<orderId, orderCode>
    public Map<Long, String> getAllOrderCodeByOrderIds(List<Long> orderIds) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseOrderIdInAndDeletedAtIsNull(orderIds);

        if (orders == null || orders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrder::getOrderCode
                ));
    }

    // return: Map<returnItemId, productId>
    public Map<Long, Long> getProductIdByReturnItemId(Map<Long, Long> orderItemIdByReturnItemId) {
        // List<orderItemId>
        List<Long> orderItemIds = orderItemIdByReturnItemId.values().stream().toList();

        // Map<orderItemId, List<returnItemId>>
        Map<Long, List<Long>> returnItemIdsByOrderItemId = orderItemIdByReturnItemId.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        // List<FranchiseOrderItem>
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(orderItemIds);

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = items.stream()
                .collect(Collectors.toMap(
                        FranchiseOrderItem::getFranchiseOrderItemId,
                        FranchiseOrderItem::getProductId
                ));

        if (items.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItemIdByReturnItemId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> productIdByOrderItemId.get(entry.getValue())
                ));
    }

    // return: FranchiseOrderDetailCommand
    public FranchiseOrderDetailCommand getOrderByOrderId(Long franchiseId, Long userId, Long orderId) {
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseIdAndUserIdAndFranchiseOrderIdAndDeletedAtIsNull(franchiseId, userId, orderId)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));

        return FranchiseOrderDetailCommand.from(order);
    }

    // return: Map<orderItemId, productId>
    public Map<Long, Long> getProductIdByOrderItemId(List<Long> orderItemIds) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdInAndDeletedAtIsNull(orderItemIds);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(
                        FranchiseOrderItem::getFranchiseOrderItemId,
                        FranchiseOrderItem::getProductId
                ));
    }

    public List<FranchiseOrderForTransitResponse> getOrdersForOutbound(List<Long> orderIds) {

        if(orderIds == null || orderIds.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        List<FranchiseOrderItem> allItems = franchiseOrderItemRepository
                .findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(
                        orderIds
                );

        if (allItems.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        Map<FranchiseOrder, List<FranchiseOrderItem>> itemsByOrder = allItems.stream()
                .collect(Collectors.groupingBy(FranchiseOrderItem::getFranchiseOrder));

        long requestedCount = orderIds.stream().distinct().count();

        if (itemsByOrder.size() != requestedCount) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        return itemsByOrder.entrySet().stream()
                .map(entry -> {
                    FranchiseOrder order = entry.getKey();
                    List<FranchiseOrderItem> items = entry.getValue();

                    List<FranchiseOrderForTransitResponse.OrderItemForTransit> itemResponses = items.stream()
                            .map(item -> new FranchiseOrderForTransitResponse.OrderItemForTransit(
                                    item.getProductId(),
                                    item.getQuantity()
                            ))
                            .toList();

                    return new FranchiseOrderForTransitResponse(
                            order.getFranchiseOrderId(),
                            order.getOrderCode(),
                            itemResponses,
                            order.getFranchiseId(),
                            order.getCreatedAt(),
                            order.getDeliveryDate()
                    );
                })
                .toList();
    }

    public List<FranchiseOrderForTransitResponse> getOrdersForTransit(List<Long> orderIds) {

        if(orderIds == null || orderIds.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        List<FranchiseOrderItem> allItems = franchiseOrderItemRepository
                .findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(
                        orderIds
                );

        if (allItems.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        Map<FranchiseOrder, List<FranchiseOrderItem>> itemsByOrder = allItems.stream()
                .collect(Collectors.groupingBy(FranchiseOrderItem::getFranchiseOrder));

        long requestedCount = orderIds.stream().distinct().count();

        if (itemsByOrder.size() != requestedCount) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        return itemsByOrder.entrySet().stream()
                .map(entry -> {
                    FranchiseOrder order = entry.getKey();
                    List<FranchiseOrderItem> items = entry.getValue();

                    List<FranchiseOrderForTransitResponse.OrderItemForTransit> itemResponses = items.stream()
                            .map(item -> new FranchiseOrderForTransitResponse.OrderItemForTransit(
                                    item.getProductId(),
                                    item.getQuantity()
                            ))
                            .toList();

                    return new FranchiseOrderForTransitResponse(
                            order.getFranchiseOrderId(),
                            order.getOrderCode(),
                            itemResponses,
                            order.getFranchiseId(),
                            order.getCreatedAt(),
                            order.getDeliveryDate()
                    );
                })
                .toList();
    }

    // 대기 상태 발주 요청 조회
    // return: Map<orderId, FranchiseOrderDetailCommand> 
    public Map<Long, FranchiseOrderDetailCommand> getAllRequestedOrders() {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderStatusAndDeletedAtIsNull(FranchiseOrderStatus.PENDING);

        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrderDetailCommand::from
                ));
    }

    // return: Map<orderId, List<FranchiseOrderItemCommand>>
    public Map<Long, List<FranchiseOrderItemCommand>> getAllRequestedOrderItem(List<Long> orderIds) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderIdInAndDeletedAtIsNull(orderIds);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFranchiseOrder().getFranchiseOrderId(),
                        Collectors.mapping(FranchiseOrderItemCommand::from, Collectors.toList())
                ));
    }

    // return: Map<orderId, FranchiseOrderDetail>
    public Map<Long, FranchiseOrderDetailCommand> getAllOrders() {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByDeletedAtIsNull();

        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrderDetailCommand::from
                ));
    }

    public List<FranchiseOrderForTransitResponse> getOrdersForAssignVehicle() {
        List<FranchiseOrderStatus> statuses = List.of(
                FranchiseOrderStatus.PARTIAL,
                FranchiseOrderStatus.ACCEPTED
        );

        List<FranchiseOrder> unassignedOrders =
                franchiseOrderRepository.getFranchiseOrderByFranchiseOrderStatus(statuses);

        if (unassignedOrders == null || unassignedOrders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        List<Long> unassignedOrderIds = unassignedOrders.stream()
                .map(FranchiseOrder::getFranchiseOrderId)
                .toList();

        List<FranchiseOrderItem> unassignedOrderItems =
                franchiseOrderItemRepository.findAllByFranchiseOrderFranchiseOrderIdIn(unassignedOrderIds);

        Map<Long, List<FranchiseOrderForTransitResponse.OrderItemForTransit>> itemsByOrderId = unassignedOrderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFranchiseOrder().getFranchiseOrderId(),
                        Collectors.mapping(item -> new FranchiseOrderForTransitResponse.OrderItemForTransit(
                                item.getProductId(),
                                item.getQuantity()
                        ), Collectors.toList())
                ));

        return unassignedOrders.stream()
                .map(order -> new FranchiseOrderForTransitResponse(
                        order.getFranchiseOrderId(),
                        order.getOrderCode(),
                        itemsByOrderId.getOrDefault(order.getFranchiseOrderId(), List.of()),
                        order.getFranchiseId(),
                        order.getCreatedAt(),
                        order.getDeliveryDate()
                ))
                .toList();
    }

    // 발주 상태 SHIPPING_PENDING으로 수정
    // return: Map<orderId, FranchiseOrderCommand>
    public Map<Long, FranchiseOrderCommand> updateShippingPending(Set<String> orderCodes) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeInAndDeletedAtIsNull(orderCodes);

        if (orders == null || orders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        // Set<FranchiseOrder OrderCode>
        Set<String> existingOrderCodes = orders.stream().map(FranchiseOrder::getOrderCode).collect(Collectors.toSet());

        if (!existingOrderCodes.containsAll(orderCodes)) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.DATA_OMISSION);
        }

        orders.forEach(FranchiseOrder::updateStatusToShippingPending);

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrderCommand::from
                ));
    }

    // 본사의 발주 조회
    public FranchiseOrderDetailCommand getOrderByHQ(String orderCode) {
        FranchiseOrder order = franchiseOrderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));

        return FranchiseOrderDetailCommand.from(order);
    }

    // 본사의 가맹점 발주 요청 취소
    // return: Map<orderCode, FranchiseOrderStatus>
    public Map<String, FranchiseOrderStatus> cancelFranchiseOrder(List<HQFranchiseOrderCancelRequest> requests) {
        // Set<orderCode>
        Set<String> orderCodes = requests.stream().map(HQFranchiseOrderCancelRequest::orderCode).collect(Collectors.toSet());

        // Map<orderCode, canceledReason>
        Map<String, String> reasonByOrderCode = requests.stream()
                .collect(Collectors.toMap(
                        HQFranchiseOrderCancelRequest::orderCode,
                        HQFranchiseOrderCancelRequest::canceledReason
                ));

        // List<FranchiseOrder>
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeInAndDeletedAtIsNull(orderCodes);

        if (orders == null || orders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        // Set<FranchiseOrder orderCode>
        Set<String> existingOrderCodes = orders.stream().map(FranchiseOrder::getOrderCode).collect(Collectors.toSet());

        if (!existingOrderCodes.containsAll(orderCodes)) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.DATA_OMISSION);
        }

        orders.forEach(order -> {
            order.cancelOrderByHQ(reasonByOrderCode.get(order.getOrderCode()));
        });

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getOrderCode,
                        FranchiseOrder::getOrderStatus
                ));
    }

    // return: Map<orderId, FranchiseOrderDetailCommand>
    public Map<Long, FranchiseOrderDetailCommand> getOrdersByOrderCode(List<String> orderCodes) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeInAndDeletedAtIsNull(orderCodes);

        if (orders == null || orders.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        // Set<orderCode>
        Set<String> requestedOrderCodes = new HashSet<>(orderCodes);

        // Set<orderCode>
        Set<String> existingOrderCodes = orders.stream()
                .map(FranchiseOrder::getOrderCode)
                .collect(Collectors.toSet());

        if (!existingOrderCodes.containsAll(requestedOrderCodes)) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.DATA_OMISSION);
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrderDetailCommand::from
                ));
    }

    public void updateDeliveryStatus(List<String> orderCodes, FranchiseOrderStatus orderStatus) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeInAndDeletedAtIsNull(orderCodes);

        if (orders == null || orders.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND);
        }

        Set<String> existingOrderCodes = orders.stream().map(FranchiseOrder::getOrderCode).collect(Collectors.toSet());
        Set<String> requestedOrderCodes = new HashSet<>(orderCodes);

        if (!existingOrderCodes.containsAll(requestedOrderCodes)) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.DATA_OMISSION);
        }

        orders.forEach(order -> order.updateStatus(orderStatus));
    }
}
