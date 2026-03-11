package com.chaing.api.facade.factory;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.command.HQOrderItemCommand;
import com.chaing.domain.orders.dto.info.HQOrderCommand;
import com.chaing.domain.orders.dto.request.FactoryOrderRequest;
import com.chaing.domain.orders.dto.response.FactoryOrderResponse;
import com.chaing.domain.orders.dto.response.FactoryOrderUpdateResponse;
import com.chaing.domain.orders.enums.HQOrderStatus;
import com.chaing.domain.orders.exception.OrderErrorCode;
import com.chaing.domain.orders.exception.OrderException;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.users.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryFacade {

    private final HQOrderService hqOrderService;
    private final ProductService productService;
    private final UserManagementService userManagementService;

    // 발주 전체/대기 조회
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
        log.info("orders: {}", ordersByOrderId);
        // 발주 존재하지 않을 시 빈 배열 반환
        if (ordersByOrderId == null || ordersByOrderId.isEmpty()) {
            return List.of();
        }

        // List<orderId>
        List<Long> orderIds = ordersByOrderId.keySet().stream().toList();

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
                        entry -> userManagementService.getUsernameByUserId(entry.getValue())
                ));

        // Map<orderId, phoneNumber>
        Map<Long, String> phoneNumberByOrderId = userIdByOrderId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userManagementService.getPhoneNumberByUserId(entry.getValue())
                ));

        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemIdsByOrderId(orderIds);

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
        // 재고 확인

        // Map<orderCode, HQOrderStatus>
        Map<String, HQOrderStatus> orderStatusByOrderCode = hqOrderService.updateOrders(request.orderCodes(), isAccept);

        // 반환
        return orderStatusByOrderCode.entrySet().stream()
                .map(entry -> FactoryOrderUpdateResponse.builder()
                        .orderCode(entry.getKey())
                        .status(entry.getValue())
                        .build())
                .toList();
    }
}
