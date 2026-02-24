package com.chaing.domain.orders.service;

import com.chaing.core.dto.returns.request.OrderItemIdAndSerialCode;
import com.chaing.domain.orders.dto.command.FranchiseOrderCreateCommand;
import com.chaing.domain.orders.dto.command.FranchiseOrderUpdateCommand;
import com.chaing.domain.orders.dto.info.FranchiseOrderItemInfo;
import com.chaing.domain.orders.entity.FranchiseOrder;
import com.chaing.domain.orders.entity.FranchiseOrderItem;
import com.chaing.domain.orders.enums.FranchiseOrderStatus;
import com.chaing.domain.orders.exception.FranchiseOrderErrorCode;
import com.chaing.domain.orders.exception.FranchiseOrderException;
import com.chaing.domain.orders.repository.FranchiseOrderItemRepository;
import com.chaing.domain.orders.repository.FranchiseOrderRepository;
import com.chaing.domain.orders.support.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FranchiseOrderService {

    private final FranchiseOrderRepository franchiseOrderRepository;
    private final FranchiseOrderItemRepository franchiseOrderItemRepository;

    // 가맹점 발주 목록 조회
    public List<FranchiseOrder> getAllOrders(Long franchiseId, String username) {
        return franchiseOrderRepository.findAllByFranchiseIdAndUsername(franchiseId, username);
    }

    // 발주 번호에 따른 가맹점 특정 발주 조회
    public FranchiseOrder getOrder(Long franchiseId, String username, String orderCode) {
        return franchiseOrderRepository.findByFranchiseIdAndUsernameAndOrderCode(franchiseId, username, orderCode)
                .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
    }

    // 가맹점의 발주 수정
    public void updateOrder(FranchiseOrder order, FranchiseOrderUpdateCommand request) {
        // 발주 상태가 PENDING 아니면 예외 발생
        if (order.getOrderStatus() != FranchiseOrderStatus.PENDING) {
            throw new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_INVALID_STATUS);
        }

        // request.items를 뽑아내서 따로 수정
        for (FranchiseOrderItemInfo item : request.items()) {
            FranchiseOrderItem orderItem = franchiseOrderItemRepository.findByFranchiseOrder_FranchiseOrderIdAndSerialCode(order.getFranchiseOrderId(), item.serialCode())
                    .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.ORDER_NOT_FOUND));
            orderItem.update(item);
        }

        // 발주 수정
        order.update(request);
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
        List<FranchiseOrderItem> orderItems = request.items().stream()
                        .map(item -> {
                            FranchiseOrderItem requestOrderItem = productInfos.stream()
                                    .filter(info -> item.productCode().equals(info.productCode()))
                                    .map(info -> {
                                        return FranchiseOrderItem.builder()
                                                .franchiseOrder(order)
                                                .serialCode(info.serialCode())
                                                .quantity(item.quantity())
                                                .unitPrice(info.unitPrice())
                                                .totalPrice(info.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                                                .build();
                                    })
                                    .findAny()
                                    .orElseThrow(() -> new FranchiseOrderException(FranchiseOrderErrorCode.PRODUCT_NOT_FOUND));
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
        List<FranchiseOrder> orders = franchiseOrderRepository.findAllByFranchiseOrderIdIn(orderIds);

        return orders.stream()
                .collect(Collectors.toMap(
                        FranchiseOrder::getFranchiseOrderId,
                        FranchiseOrder::getOrderCode
                ));
    }

    // orderItemId에 대한 serialCode 반환
    public List<OrderItemIdAndSerialCode> getSerialCodes(List<Long> orderItemIds) {
        // orderItemId에 해당하는 serialCode 조회
        List<FranchiseOrderItem> items = franchiseOrderItemRepository.findAllByFranchiseOrderItemIdIn(orderItemIds);
        System.out.println("serialCode 포함한 items: " + items.get(0).getSerialCode());

        return items.stream()
                .map(item -> {
                    return OrderItemIdAndSerialCode.builder()
                            .orderItemId(item.getFranchiseOrderItemId())
                            .serialCode(item.getSerialCode())
                            .build();
                })
                .toList();
    }
}
