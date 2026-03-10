package com.chaing.domain.orders.service;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.command.HQOrderCancelCommand;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.info.HQOrderItemCommand;
import com.chaing.domain.orders.dto.request.FactoryOrderRequest;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.domain.orders.dto.request.HQOrderItemCreateCommand;
import com.chaing.domain.orders.dto.request.HQOrderItemUpdateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.response.HQOrderForTransitResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.repository.HeadOfficeOrderItemRepository;
import com.chaing.domain.orders.repository.HeadOfficeOrderRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HQOrderService {

    private final HeadOfficeOrderRepository orderRepository;
    private final HeadOfficeOrderItemRepository orderItemRepository;
    private final HQOrderCodeGenerator generator;

    // 발주 정보 조회
    public Map<Long, HQOrderCommand> getAllOrders() {
        List<HeadOfficeOrder> orders = orderRepository.findAllByDeletedAtIsNull();

        if (orders == null || orders.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND);
        }

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderCommand::from
                ));
    }

    // 특정 발주 정보 조회
    public HQOrderCommand getOrder(String orderCode) {
        HeadOfficeOrder order = orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        return HQOrderCommand.from(order);
    }

    // 발주 제품 productId 조회
    // List<productId>
    public List<Long> getOrderItemProductId(Long hqId, Long orderId) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderIdAndDeletedAtIsNull(orderId);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .map(HeadOfficeOrderItem::getProductId)
                .toList();
    }

    // 발주 제품 수정
    public List<HQOrderItemCommand> updateOrderItems(Long userId, String orderCode, HQOrderUpdateRequest request, Map<String, ProductInfo> productInfoByProductCode) {
        // 발주 정보
        HeadOfficeOrder order = orderRepository.findByUserIdAndOrderCodeAndOrderStatusAndDeletedAtIsNull(userId, orderCode, HQOrderStatus.PENDING)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.INVALID_STATUS));

        // 원본 발주 제품 정보
        List<HeadOfficeOrderItem> items = orderItemRepository.findAllByHeadOfficeOrder_UserIdAndHeadOfficeOrder_OrderCodeAndDeletedAtIsNull(userId, orderCode);

        if (items == null || items.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // Map<productId, HeadOfficeOrderItem>
        Map<Long, HeadOfficeOrderItem> existingItems = items.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrderItem::getProductId,
                        Function.identity()
                ));

        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productInfoByProductCode.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productId,
                        Function.identity()
                ));

        // Map<productId, HQOrderItemUpdateRequest> - 요청 정보
        Map<Long, HQOrderItemUpdateRequest> requestMap = request.items().stream()
                .collect(Collectors.toMap(
                        info -> {
                            if (productInfoByProductCode.get(info.productCode()) == null) {
                                throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
                            }

                            return productInfoByProductCode.get(info.productCode()).productId();
                        },
                        Function.identity()
                ));

        // 삭제 대상
        List<HeadOfficeOrderItem> deletedItems = items.stream()
                .filter(item -> !requestMap.containsKey(item.getProductId()))
                .toList();

        // 수정/추가
        List<HeadOfficeOrderItem> upsertItems = requestMap.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    HeadOfficeOrderItem item = existingItems.get(productId);
                    HQOrderItemUpdateRequest updateRequest = entry.getValue();
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    if (item != null) {
                        // 수정
                        item.update(updateRequest.quantity());
                        return item;
                    } else {
                        // 추가
                        return HeadOfficeOrderItem.builder()
                                .headOfficeOrder(order)
                                .productId(productId)
                                .quantity(updateRequest.quantity())
                                .unitPrice(productInfo.costPrice())
                                .totalPrice(productInfo.costPrice().multiply(BigDecimal.valueOf(updateRequest.quantity())))
                                .build();
                    }
                })
                .toList();

        // 수정사항 반영
        deletedItems.forEach(HeadOfficeOrderItem::delete);
        orderItemRepository.saveAll(upsertItems);

        // 발주 정보 수정
        Integer totalQuantity = upsertItems.stream()
                .map(HeadOfficeOrderItem::getQuantity)
                .reduce(0, Integer::sum);
        BigDecimal totalPrice = upsertItems.stream()
                .map(HeadOfficeOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.updateTotalQuantity(totalQuantity);
        order.updateTotalPrice(totalPrice);

        // 반환
        return upsertItems.stream()
                .map(HQOrderItemCommand::from)
                .toList();
    }

    // 발주 정보 수정
    public HQOrderCommand updateOrder(String orderCode, @NotNull LocalDateTime manufactureDate) {
        // 발주 정보 조회
        HeadOfficeOrder order = orderRepository.findByOrderCodeAndDeletedAtIsNull(orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // 수정
        order.update(manufactureDate);

        // 반환
        return HQOrderCommand.from(order);
    }

    // return: Map<orderCode, HQOrderStatus>
    public HQOrderCancelCommand cancel(Long userId, String orderCode) {
        // 발주 정보 조회
        HeadOfficeOrder order = orderRepository.findByUserIdAndOrderCodeAndDeletedAtIsNull(userId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // 취소
        order.cancel();

        // 반환
        return HQOrderCancelCommand.from(order);
    }

    // 발주 생성
    public HQOrderCommand createOrder(Long userId, HQOrderCreateRequest request, String hqCode, Map<Long, ProductInfo> productInfoByProductId) {
        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));

        // 검증
        request.items().forEach(item -> {
            if (!productInfoByProductCode.containsKey(item.productCode())) {
                throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
            }
        });

        // totalPrice
        BigDecimal totalPrice = request.items().stream()
                .map(item -> {
                    ProductInfo productInfo = productInfoByProductCode.get(item.productCode());

                    return productInfo.costPrice().multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // totalQuantity
        Integer totalQuantity = request.items().stream()
                .map(HQOrderItemCreateCommand::quantity)
                .reduce(0, Integer::sum);

        // 발주 생성
        HeadOfficeOrder order = HeadOfficeOrder.builder()
                .orderCode(generator.generate(hqCode))
                .userId(userId)
                .manufactureDate(request.manufactureDate())
                .description(request.description())
                .isRegular(request.isRegular())
                .totalQuantity(totalQuantity)
                .totalAmount(totalPrice)
                .build();

        // 저장
        orderRepository.save(order);

        // 반환
        return HQOrderCommand.from(order);
    }

    // 발주 제품 생성
    public List<HQOrderItemCommand> createOrderItems(Long orderId, Map<Long, ProductInfo> productInfoByProductId, List<HQOrderItemCreateCommand> items) {
        // 발주 조회
        HeadOfficeOrder order = orderRepository.findByHeadOfficeOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        // Map<productCode, ProductInfo>
        Map<String, ProductInfo> productInfoByProductCode = productInfoByProductId.values().stream()
                .collect(Collectors.toMap(
                        ProductInfo::productCode,
                        Function.identity()
                ));

        // 발주 제품 생성
        List<HeadOfficeOrderItem> orderItems = items.stream()
                .map(item -> {
                            String productCode = item.productCode();
                            Integer quantity = item.quantity();
                            ProductInfo productInfo = productInfoByProductCode.get(productCode);
                            BigDecimal unitPrice = productInfo.costPrice();

                            return HeadOfficeOrderItem.builder()
                                    .headOfficeOrder(order)
                                    .productId(productInfo.productId())
                                    .quantity(quantity)
                                    .unitPrice(unitPrice)
                                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                                    .build();
                        }
                )
                .toList();

        // 발주 제품 저장
        orderItemRepository.saveAll(orderItems);

        // 반환
        return HQOrderItemCommand.ofList(orderItems, productInfoByProductId);
    }

    public Map<Long, HQOrderCommand> getAllPendingOrders() {
        List<HeadOfficeOrder> orders = orderRepository.findAllByOrderStatusAndDeletedAtIsNull(HQOrderStatus.PENDING);

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderCommand::from
                ));
    }

    // 대기 상태 발주 제품 정보 조회
    // return: Map<orderId, List<HQOrderItemCommand>>
    public Map<Long, List<com.chaing.domain.orders.dto.command.HQOrderItemCommand>> getOrderItemIdsByOrderId(List<Long> orderIds) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HeadOfficeOrderIdIn(orderIds);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getHeadOfficeOrder().getHeadOfficeOrderId(),
                        Collectors.mapping(com.chaing.domain.orders.dto.command.HQOrderItemCommand::from, Collectors.toList())
                ));
    }

    // return: Map<orderItemId, productId>
    public Map<Long, Long> getProductIdsByOrderItemIds(List<Long> orderItemIds) {
        List<HeadOfficeOrderItem> items = orderItemRepository.findAllByHeadOfficeOrderItemIdInAndDeletedAtIsNull(orderItemIds);

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
    public Map<Long, HQOrderCommand> getAllOrdersByFactory() {
        List<HeadOfficeOrder> orders = orderRepository.findAll();

        return orders.stream()
                .collect(Collectors.toMap(
                        HeadOfficeOrder::getHeadOfficeOrderId,
                        HQOrderCommand::from
                ));
    }

    // 발주 접수/반려
    public Map<String, HQOrderStatus> updateOrderStatus(FactoryOrderRequest request) {
        List<HeadOfficeOrder> orders = orderRepository.findAllByOrderCodeInAndDeletedAtIsNull(request.orderCodes());

        if (orders == null || orders.isEmpty() || orders.size() != request.orderCodes().size()) {
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
