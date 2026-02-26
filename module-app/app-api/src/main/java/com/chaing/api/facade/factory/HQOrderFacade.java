package com.chaing.api.facade.factory;

import com.chaing.domain.orders.dto.info.HQOrderInfo;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.core.fake.FakeProductService;
import com.chaing.domain.orders.service.HQOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HQOrderFacade {

    private final HQOrderService hqOrderService;

    private final FakeProductService fakeProductService;

    public List<HQOrderResponse> getAllOrders(String username) {
        // hqId username으로 꺼내오는 로직 추가
        Long hqId = 10L;

        // 발주 정보 조회
        // Map<orderId, HQOrderInfo>
        Map<Long, HQOrderInfo> orderInfos = hqOrderService.getAllOrders(hqId, username);

        // 발주 제품 정보 조회
        List<Long> orderIds = orderInfos.keySet().stream().toList();
        // 1. Map<orderId, List<productId>>
        Map<Long, List<Long>> productIdByOrderId = hqOrderService.getAllOrderItems(hqId, orderIds);
        // 2. Map<productId, List<ProductInfo>> productId 별 제품 정보 조회
        List<Long> productIds = productIdByOrderId.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        Map<Long, List<ProductInfo>> productInfoByProductId = fakeProductService.getProducts(productIds);
        // 3. Map<orderId, quantity> orderId 별 수량 계산
        Map<Long, Integer> quantityByOrderId = productInfoByProductId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));

        // 반환
        return productIdByOrderId.keySet().stream()
                .map(orderId -> HQOrderResponse.builder()
                        .orderCode(orderInfos.get(orderId).orderCode())
                        .status(orderInfos.get(orderId).status())
                        .quantity(quantityByOrderId.get(orderId))
                        .username(orderInfos.get(orderId).username())
                        .phoneNumber(orderInfos.get(orderId).phoneNumber())
                        .requestedDate(orderInfos.get(orderId).requestedDate())
                        .manufacturedDate(orderInfos.get(orderId).manufacturedDate())
                        .storedDate(orderInfos.get(orderId).storedDate())
                        .productCode(productInfoByProductId.get(orderId).getFirst().productCode())
                        .build())
                .toList();
    }
}
