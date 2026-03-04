package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.info.HQOrderItemInfo;
import com.chaing.domain.orders.dto.request.FactoryOrderRequest;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateInfo;
import com.chaing.domain.orders.dto.request.HQOrderItemUpdateRequest;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.repository.HeadOfficeOrderItemRepository;
import com.chaing.domain.orders.repository.HeadOfficeOrderRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HQOrderService {

    private final HeadOfficeOrderRepository orderRepository;
    private final HeadOfficeOrderItemRepository orderItemRepository;
    private final HQOrderCodeGenerator generator;

    // 발주 정보 조회
    // hqId, username으로 order 조회
    public Map<Long, HQOrderInfo> getAllOrders(Long hqId, String username) {
        List<HeadOfficeOrder> orders = orderRepository.findAllByHqIdAndUsername(hqId, username);

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderInfo::from
                ));
    }

    // 발주 제품 정보 조회
    // orderId로 orderItem 조회
    // Map<orderId, List<productId>>
    public Map<Long, List<Long>> getAllOrderItemProductId(Long hqId, List<Long> orderIds) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderIdIn(hqId, orderIds);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getHeadOfficeOrder().getHeadOfficeOrderId(),
                        Collectors.mapping(
                                HeadOfficeOrderItem::getProductId,
                                Collectors.toList()
                        )
                ));
    }

    // 특정 발주 정보 조회
    public HQOrderInfo getOrder(Long hqId, String orderCode) {
        HeadOfficeOrder order = orderRepository.findByHqIdAndOrderCode(hqId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        return HQOrderInfo.from(order);
    }

    // 발주 제품 productId 조회
    // List<productId>
    public List<Long> getOrderItemProductId(Long hqId, Long orderId) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .map(HeadOfficeOrderItem::getProductId)
                .toList();
    }

    // 발주 제품 수정
    public List<HQOrderItemInfo> updateOrderItems(Long hqId, String orderCode, List<HQOrderItemUpdateRequest> request, Map<Long, ProductInfo> productInfoByProductId) {
        // 발주 조회
        HeadOfficeOrder order = orderRepository.findByHqIdAndOrderCode(hqId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // Map<productId, HeadOfficeOrderItem> 발주 제품 조회
        Map<Long, HeadOfficeOrderItem> items = orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(hqId, orderCode).stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrderItem::getProductId,
                        Function.identity()
                ));

        if (items.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 발주 수정
        // 1. 요청에 대한 Map<productId, quantity>
        Map<Long, Integer> requestedItems = request.stream()
                .collect(Collectors.toMap(
                        HQOrderItemUpdateRequest::productId,
                        HQOrderItemUpdateRequest::quantity
                ));
        // 2. 추가
        requestedItems.forEach((productId, quantity) -> {
            if (!items.containsKey(productId)) {
                ProductInfo productInfo = productInfoByProductId.get(productId);
                if (productInfo == null) {
                    throw new HQOrderException(HQOrderErrorCode.INVALID_INPUT);
                }

                // 추가
                HeadOfficeOrderItem addedItem = HeadOfficeOrderItem.builder()
                        .headOfficeOrder(order)
                        .productId(productId)
                        .quantity(quantity)
                        .unitPrice(productInfo.costPrice())
                        .totalPrice(productInfo.costPrice().multiply(BigDecimal.valueOf(quantity)))
                        .build();

                orderItemRepository.save(addedItem);
                items.put(productId, addedItem);
            } else {
                // 업데이트
                items.get(productId).update(productId, quantity);
            }
        });
        // 3. 삭제
        List<Long> toDelete = new ArrayList<>();
        items.forEach((productId, headOfficeOrderItem) -> {
            if (!requestedItems.containsKey(productId)) {
                toDelete.add(productId);
            }
        });
        toDelete.forEach(productId -> {
            items.get(productId).delete();
            items.remove(productId);
        });

        // 반환
        return items.values().stream()
                .map(item -> HQOrderItemInfo.of(item, productInfoByProductId))
                .toList();
    }

    // 발주 정보 수정
    public HQOrderInfo updateOrder(Long hqId, String orderCode, @NotNull LocalDateTime manufactureDate) {
        // 발주 정보 조회
        HeadOfficeOrder order = orderRepository.findByHqIdAndOrderCode(hqId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // 수정
        order.update(manufactureDate);

        // 반환
        return HQOrderInfo.from(order);
    }

    public Map<String, HQOrderStatus> cancel(Long hqId, String orderCode) {
        // 발주 정보 조회
        HeadOfficeOrder order = orderRepository.findByHqIdAndOrderCode(hqId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // 취소
        order.cancel();

        // 반환
        Map<String, HQOrderStatus> result = new HashMap<>();
        result.put(order.getOrderCode(), order.getOrderStatus());

        return result;
    }

    // 발주 생성
    public HQOrderInfo createOrder(Long hqId, HQOrderCreateRequest request, Integer totalQuantity, BigDecimal totalAmount) {
        // 발주 생성
        HeadOfficeOrder order = HeadOfficeOrder.builder()
                .orderCode(generator.generate())
                .hqId(hqId)
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .manufactureDate(request.manufactureDate())
                .description(request.description())
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .isRegular(request.isRegular())
                .build();

        // 저장
        orderRepository.save(order);

        // 반환
        return HQOrderInfo.from(order);
    }

    // 발주 제품 생성
    public List<HQOrderItemInfo> createOrderItems(Long orderId, Map<Long, ProductInfo> productInfoByProductId, List<HQOrderItemCreateInfo> items) {
        // 발주 조회
        HeadOfficeOrder order = orderRepository.findByHeadOfficeOrderId(orderId)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // 발주 제품 생성
        List<HeadOfficeOrderItem> orderItems = items.stream()
                .map(item -> HeadOfficeOrderItem.builder()
                        .headOfficeOrder(order)
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .unitPrice(productInfoByProductId.get(item.productId()).costPrice())
                        .totalPrice(productInfoByProductId.get(item.productId()).costPrice().multiply(BigDecimal.valueOf(item.quantity())))
                        .build())
                .toList();

        // 발주 제품 저장
        orderItemRepository.saveAll(orderItems);

        // 반환
        return HQOrderItemInfo.ofList(orderItems, productInfoByProductId);
    }

    public Map<Long, HQOrderInfo> getAllPendingOrders() {
        List<HeadOfficeOrder> orders = orderRepository.findAllByOrderStatus(HQOrderStatus.PENDING);

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderInfo::from
                ));
    }

    // 대기 상태 발주 제품 정보 조회
    // return: Map<orderId, List<orderItemId>>
    public Map<Long, List<Long>> getOrderItemIdsByOrderIdAndStatus(List<Long> orderIds) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(orderIds);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getHeadOfficeOrder().getHeadOfficeOrderId(),
                        Collectors.mapping(HeadOfficeOrderItem::getHeadOfficeOrderItemId, Collectors.toList())
                ));
    }

    // return: Map<orderItemId, productId>
    public Map<Long, Long> getProductIdsByOrderItemIds(List<Long> orderItemIds) {
        List<HeadOfficeOrderItem> items = orderItemRepository.findAllByHeadOfficeOrderItemIdIn(orderItemIds);

        if (items == null || items.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrderItem::getHeadOfficeOrderItemId,
                        HeadOfficeOrderItem::getProductId
                ));
    }

    // 발주 전체 조회
    public Map<Long, HQOrderInfo> getAllOrdersByFactory() {
        List<HeadOfficeOrder> orders = orderRepository.findAll();

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderInfo::from
                ));
    }

    // 발주 접수/반려
    public Map<String, HQOrderStatus> updateOrderStatus(FactoryOrderRequest request) {
        List<HeadOfficeOrder> orders = orderRepository.findAllByOrderCodeIn(request.orderCodes());

        if (orders == null || orders.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND);
        }

        if (request.isAccept()) {
            orders.forEach(HeadOfficeOrder::accept);
        } else {
            orders.forEach(HeadOfficeOrder::reject);
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getOrderCode,
                        HeadOfficeOrder::getOrderStatus
                ));
    }
}
