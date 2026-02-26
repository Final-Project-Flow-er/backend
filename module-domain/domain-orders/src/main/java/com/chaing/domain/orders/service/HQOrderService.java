package com.chaing.domain.orders.service;

import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import com.chaing.domain.orders.repository.HeadOfficeOrderItemRepository;
import com.chaing.domain.orders.repository.HeadOfficeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HQOrderService {

    private final HeadOfficeOrderRepository orderRepository;
    private final HeadOfficeOrderItemRepository orderItemRepository;

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
    public Map<Long, List<Long>> getAllOrderItems(Long hqId, List<Long> orderIds) {
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

    // 발주 정보 조회
    public HQOrderInfo getOrder(Long hqId, String orderCode) {
        HeadOfficeOrder order = orderRepository.findByHqIdAndOrderCode(hqId, orderCode)
                .orElseThrow(() -> new HQOrderException(HQOrderErrorCode.ORDER_NOT_FOUND));

        return HQOrderInfo.from(order);
    }

    // 발주 제품 productId 조회
    // List<productId>
    public List<Long> getOrderItems(Long hqId, Long orderId) {
        List<HeadOfficeOrderItem> orderItems = orderItemRepository.findAllByHeadOfficeOrder_HqIdAndHeadOfficeOrder_HeadOfficeOrderId(hqId, orderId);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new HQOrderException(HQOrderErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        return orderItems.stream()
                .map(HeadOfficeOrderItem::getProductId)
                .toList();
    }
}
