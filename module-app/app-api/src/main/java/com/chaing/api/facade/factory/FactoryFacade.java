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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryFacade {

    private final HQOrderService hqOrderService;
    private final ProductService productService;

    // 발주 전체/대기 조회
    public List<FactoryOrderResponse> getAllOrders(Boolean isAccepted) {
        // Map<orderId, HQOrderInfo>
        Map<Long, HQOrderCommand> orderByOrderId;

        if (isAccepted) {
            // 전체 발주 조회
            orderByOrderId = hqOrderService.getAllOrdersByFactory();
        } else {
            // 대기 발주 조회
            orderByOrderId = hqOrderService.getAllPendingOrders();
        }

        if (orderByOrderId.isEmpty()) {
            return List.of();
        }

        // List<orderId>
        List<Long> orderIds = orderByOrderId.keySet().stream().toList();

        // 대기 상태 발주 제품 정보 조회
        // Map<orderId, List<HQOrderItemCommand>>
        Map<Long, List<HQOrderItemCommand>> orderItemsByOrderId = hqOrderService.getOrderItemIdsByOrderId(orderIds);

        // List<orderItemId>
        List<Long> orderItemIds = orderItemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(HQOrderItemCommand::orderItemId)
                .toList();

        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = hqOrderService.getProductIdsByOrderItemIds(orderItemIds);

        // 제품 정보 조회
        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream()
                .distinct()
                .toList();
        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = productIdByOrderItemId.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        // Map<orderId, Map<productId, List<HQOrderItemCommand>>>
//        Map<Long, Map<Long, List<HQOrderItemCommand>>> orderItemByProductIdByOrderId =

        // 사원 정보 조회
        // 나중에 엔티티가 userId 갖도록 수정해야함
        String employeeNumber = "employeeNumber";

        // 반환
        return productIdByOrderItemId.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getValue();
                    Long orderItemId = entry.getKey();
//                    Long orderId = orderIdByOrderItemId.get(orderItemId);
//                    HQOrderCommand orderInfo = orderByOrderId.get(orderId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);

//                    Integer quantity = orderItemIdByOrderId.get(orderId).size();

//                    if (orderInfo == null) {
//                        throw new OrderException(OrderErrorCode.ORDER_NOT_FOUND);
//                    }

                    if (productInfo == null) {
                        throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                    }

//                    return FactoryOrderResponse.builder()
//                            .orderCode(orderInfo.orderCode())
//                            .status(orderInfo.status())
//                            .isRegular(orderInfo.isRegular())
//                            .productCode(productInfo.productCode())
//                            .productName(productInfo.productName())
//                            .quantity(quantity)
//                            .username(orderInfo.username())
//                            .phoneNumber(orderInfo.phoneNumber())
//                            .employeeNumber(employeeNumber)
//                            .requestedDate(orderInfo.requestedDate())
//                            .storedDate(orderInfo.storedDate())
//                            .build();
                    return FactoryOrderResponse.builder().build();
                })
                .toList();
    }

    // 발주 접수/반려
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<FactoryOrderUpdateResponse> updateOrders(@Valid FactoryOrderRequest request) {
        // 재고 확인

        // 접수/반려
        // Map<orderCode, HQOrderStatus>
        Map<String, HQOrderStatus> orderStatusByOrderCode = hqOrderService.updateOrderStatus(request);

        // 반환
        return orderStatusByOrderCode.entrySet().stream()
                .map(entry -> FactoryOrderUpdateResponse.builder()
                        .orderCode(entry.getKey())
                        .status(entry.getValue())
                        .build())
                .toList();
    }
}
