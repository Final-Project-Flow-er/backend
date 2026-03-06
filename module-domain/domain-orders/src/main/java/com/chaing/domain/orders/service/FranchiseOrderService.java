package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequestItem;
import com.chaing.domain.orders.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FranchiseOrderService {

    private final FranchiseOrderRepository franchiseOrderRepository;
    private final FranchiseOrderItemRepository franchiseOrderItemRepository;

    // 가맹점 발주 목록 조회
    public Map<Long, FranchiseOrderCommand> getAllOrders(Long franchiseId, Long userId) {
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseIdAndUserId(franchiseId, userId);

        if (orders == null || orders.isEmpty()) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId, FranchiseOrderCommand::from
                ));
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
                return FranchiseOrderItem.builder().franchiseOrder(order).productId(productId).quantity(request.quantity()).unitPrice(productInfo.retailPrice()).build();
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
                .map(item -> productInfoByProductCode.get(item.productCode()).retailPrice().multiply(BigDecimal.valueOf(item.quantity())))
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
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseIdAndUserIdAndOrderStatus(franchiseId, userId, FranchiseOrderStatus.PENDING);

        List<String> orderCodes = orders.stream().map(FranchiseOrder::getOrderCode).toList();

        return orderCodes.stream().map(orderCode ->
                new FranchiseReturnTargetResponse(orderCode, username)
        ).toList();
    }

    // 본사에서 가맹점의 발주 상태 수정
    public List<HQOrderStatusUpdateResponse> updateStatus(@Valid HQOrderUpdateStatusRequest request) {
        // 발주 조회
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByOrderCodeIn(request.orderCodes());

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
                    BigDecimal unitPrice = productInfo.retailPrice();
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
}
