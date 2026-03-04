package com.chaing.api.facade.factory;

import com.chaing.api.dto.factory.response.FactoryOrderResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.domain.orders.dto.response.FactoryPendingOrderResponse;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactoryFacade {

    private final HQOrderService hqOrderService;
    private final ProductService productService;

    public List<FactoryOrderResponse> getAllPendingOrders() {
        // 대기 상태 발주 정보 조회
        // Map<orderId, HQOrderInfo>
        Map<Long, HQOrderInfo> orderByOrderId = hqOrderService.getAllPendingOrders();

        // List<orderId>
        List<Long> orderIds = orderByOrderId.keySet().stream().toList();

        // 대기 상태 발주 제품 정보 조회
        // Map<orderId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdByOrderId = hqOrderService.getOrderItemIdsByOrderIdAndStatus(orderIds);
        // Map<orderItemId, orderId>
        Map<Long, Long> orderIdByOrderItemId = orderItemIdByOrderId.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(orderItemId -> Map.entry(orderItemId, entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // List<orderItemId>
        List<Long> orderItemIds = orderItemIdByOrderId.values().stream()
                .flatMap(List::stream)
                .toList();
        // Map<orderItemId, productId>
        Map<Long, Long> productIdByOrderItemId = hqOrderService.getProductIdsByOrderItemIds(orderItemIds);

        // 제품 정보 조회
        // List<productId>
        List<Long> productIds = productIdByOrderItemId.values().stream().toList();
        // Map<productId, ProductInfo>
        Map<Long, ProductInfo> productInfoByProductId = productService.getProductInfos(productIds);
        // Map<productId, List<orderItemId>>
        Map<Long, List<Long>> orderItemIdsByProductId = productIdByOrderItemId.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        // 사원 정보 조회
        // 나중에 엔티티가 userId 갖도록 수정해야함
        String employeeNumber = "employeeNumber";

        // 반환
        return productIdByOrderItemId.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getValue();
                    Long orderItemId = entry.getKey();
                    Long orderId = orderIdByOrderItemId.get(orderItemId);
                    HQOrderInfo orderInfo = orderByOrderId.get(orderId);
                    ProductInfo productInfo = productInfoByProductId.get(productId);

                    Integer quantity = orderItemIdByOrderId.get(orderId).size();

                    return FactoryPendingOrderResponse.builder()
                            .orderCode(orderInfo.orderCode())
                            .status(orderInfo.status())
                            .isRegular(orderInfo.isRegular())
                            .productCode(productInfo.productCode())
                            .productName(productInfo.productName())
                            .quantity(quantity)
                            .username(orderInfo.username())
                            .phoneNumber(orderInfo.phoneNumber())
                            .employeeNumber(employeeNumber)
                            .requestedDate(orderInfo.requestedDate())
                            .storedDate(orderInfo.storedDate())
                            .build();
                })
                .toList();
    }
}
