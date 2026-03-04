package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.dto.returns.request.OrderItemIdAndSerialCode;
import com.chaing.core.dto.returns.response.FranchiseOrderInfo;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.orders.dto.command.FranchiseOrderCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderDetailCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderItemCommand;
import com.chaing.domain.orders.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
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
        return franchiseOrderRepository.findAllByFranchiseIdAndUserId(franchiseId, userId).stream().collect(Collectors.toMap(FranchiseOrder::getFranchiseOrderId, FranchiseOrderCommand::from));
    }

    // 발주 번호에 따른 가맹점 특정 발주 조회
    public FranchiseOrderDetailCommand getOrder(Long franchiseId, Long userId, String orderCode) {
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCode(franchiseId, userId, orderCode).orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return FranchiseOrderDetailCommand.from(order);
    }

    // 발주 번호에 따른 발주 정보 반환
    public FranchiseOrderInfo getOrderInfo(Long franchiseId, String username, String orderCode, String franchiseCode) {
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseIdAndUserIdAndOrderCode(franchiseId, username, orderCode).orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));

        return FranchiseOrderInfo.builder().orderId(order.getFranchiseOrderId()).username(order.getUsername()).phoneNumber(order.getPhoneNumber()).franchiseCode(franchiseCode).build();
    }

    // 가맹점의 발주 수정
    public List<FranchiseOrderItemDetailResponse> updateOrder(Long orderId, Map<Long, FranchiseOrderUpdateRequest> requestByProductId, Map<Long, ProductInfo> productInfoByProductId) {
        // 발주 조회
        FranchiseOrder order = franchiseOrderRepository.findByFranchiseOrderId(orderId).orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));

        // 발주 제품 조회
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderId(orderId);

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
    public void cancelOrder(FranchiseOrder order) {
        order.cancel();
    }

    // 가맹점 발주 생성
    public FranchiseOrder createOrder(Long franchiseId, String username, FranchiseOrderCreateCommand request, List<ProductInfo> productInfos) {
        // FranchiseOrder 생성
        FranchiseOrder order = FranchiseOrder.create(franchiseId, username, request, generateOrderCode(franchiseId));
        franchiseOrderRepository.save(order);

        // FranchiseOrderItem 생성
        List<FranchiseOrderItem> orderItems = request.items().stream().map(item -> {
            FranchiseOrderItem requestOrderItem = productInfos.stream().filter(info -> item.productCode().equals(info.productCode())).map(info -> {
                return FranchiseOrderItem.builder().franchiseOrder(order).serialCode(info.serialCode()).quantity(item.quantity()).unitPrice(info.unitPrice()).totalPrice(info.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))).build();
            }).findAny().orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.PRODUCT_NOT_FOUND));
            return requestOrderItem;
        }).toList();

        order.addOrderItem(orderItems);

        order.countItems(orderItems);

        order.allocateTotalAmount(orderItems);

        franchiseOrderItemRepository.saveAll(orderItems);

        return order;
    }

    private String generateOrderCode(Long franchiseId) {
        // 나중에 redis 도입으로 일련번호 초기화 실시해야 함
        return UUID.randomUUID().toString();
    }

    // orderId에 대한 발주 코드 반환
    public Map<Long, String> getAllOrderCode(List<Long> orderIds) {
        // orderId에 해당하는 발주 조회
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseOrderIdInAndDeletedAtIsNull(orderIds);

        return orders.stream().collect(Collectors.toMap(FranchiseOrder::getFranchiseOrderId, FranchiseOrder::getOrderCode));
    }

    // orderItemId에 대한 serialCode 반환 - Map
    public List<OrderItemIdAndSerialCode> getSerialCodes(List<Long> orderItemIds) {
        // orderItemId에 해당하는 serialCode 조회
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(orderItemIds);
        System.out.println("serialCode 포함한 items: " + items.get(0).getSerialCode());

        return items.stream().map(item -> {
            return OrderItemIdAndSerialCode.builder().orderItemId(item.getFranchiseOrderItemId()).serialCode(item.getSerialCode()).build();
        }).toList();
    }

    // orderItemId에 대한 serialCode 반환 - List
    public List<String> getSerialCodeList(List<Long> orderItemIds) {
        // orderItemId에 해당하는 serialCode 조회
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(orderItemIds);

        return items.stream().map(FranchiseOrderItem::getSerialCode).toList();
    }

    // orderId, franchiseId에 대한 orderCode 반환
    public String getOrderCode(Long franchiseId, Long orderId) {
        return franchiseOrderRepository.findByFranchiseIdAndFranchiseOrderId(franchiseId, orderId).orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND)).getOrderCode();
    }

    // serialCode에 대한 orderItemId 반환
    public Long getOrderItemId(String serialCode) {
        return franchiseOrderItemRepository.findBySerialCode(serialCode).orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND)).getFranchiseOrderItemId();
    }

    // 반품 대상이 되는 발주 반환
    public List<FranchiseReturnTargetResponse> getAllTargetOrders(Long franchiseId, String username) {
        // 발주 조회
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseIdAndOrderStatus(franchiseId, FranchiseOrderStatus.PENDING);

        List<String> orderCodes = orders.stream().map(FranchiseOrder::getOrderCode).toList();

        return orderCodes.stream().map(orderCode -> {
            return new FranchiseReturnTargetResponse(orderCode, username);
        }).toList();
    }

    // orderCode로 해당 orderItem serialCode 반환 - List
    public List<String> getSerialCodesByOrderCode(Long franchiseId, String orderCode) {
        // 발주 제품 조회
        return franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseIdAndFranchiseOrder_OrderCode(franchiseId, orderCode).stream().map(FranchiseOrderItem::getSerialCode).toList();
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

    // 제품 식별 번호 반환
    // return: Map<orderItemId, serialCode>
    public Map<Long, String> getSerialCodesByOrderItemId(List<Long> orderItemIds) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(orderItemIds);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream().collect(Collectors.toMap(FranchiseOrderItem::getFranchiseOrderItemId, FranchiseOrderItem::getSerialCode));
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
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderId(orderId);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        Map<Long, List<FranchiseOrderItemCommand>> response = new HashMap<>();
        response.put(orderId, FranchiseOrderItemCommand.from(items));

        return response;
    }

    // Map<productId, List<orderItemId>>
    public Map<Long, List<Long>> getOrderItemIdsAndProductIdsByOrderId(Long orderId) {
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrder_FranchiseOrderId(orderId);

        if (items == null || items.isEmpty()) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return items.stream().collect(Collectors.groupingBy(FranchiseOrderItem::getProductId, Collectors.mapping(FranchiseOrderItem::getFranchiseOrderItemId, Collectors.toList())));
    }
}
