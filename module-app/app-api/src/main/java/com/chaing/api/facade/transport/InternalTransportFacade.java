package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.api.dto.transport.internal.response.TransportCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderForTransitResponse;
import com.chaing.domain.orders.service.HQOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.service.InternalTransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportFacade {

    private final InternalTransportService transportService;
    private final HQOrderService hQOrderService;
    private final ProductService productService;

    // 운송 가능 차량 리스트 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {
        List<com.chaing.domain.transports.dto.response.AvailableVehicleResponse> domainResponses = transportService.getAvailableVehicle();

        // API용 DTO로 변환
        return domainResponses.stream()
                .map(res -> new AvailableVehicleResponse(
                        res.vehicleId(),
                        res.vehicleNumber(),
                        res.maxLoad(),
                        res.currentWeight(),
                        res.availableWeight()
                ))
                .toList();
    }

    // 차량 배정
    @Transactional
    public void assignVehicle(VehicleAssignmentRequest request) {

        // 발주 도메인
        // 발주 Id, 중량 정보 받아오기
        List<HQOrderForTransitResponse> orders = hQOrderService.getOrdersForTransit(request.orderIds());

        // 상품 Id 추출
        List<Long> productIds = orders.stream()
                .flatMap(order -> order.items().stream())
                .map(HQOrderForTransitResponse.OrderItemForTransit::productId)
                .distinct()
                .toList();

        // 상품 무게와 정보 조회
        Map<Long, Integer> weightMap = productService.getWeightsByProductIds(productIds);

        // 발주 정보 dto 타입 변환(가공)
        List<OrderInfo> orderInfos =orders.stream()
                .map(order -> {
                    long orderWeight = order.items().stream()
                            .mapToLong(item -> (long) weightMap.getOrDefault(item.productId(), 0) *item.quantity())
                            .sum();
                    return new OrderInfo(order.orderId(), order.orderCode(), orderWeight);
                })
                .toList();

        // 선택된 발주의 총 무게 계산
        Long totalWeight = orderInfos.stream()
                .mapToLong(OrderInfo::weight)
                .sum();

        // 외부 운송 모듈
        // 송장 번호 가져오기
        Map<String, String> trackingMap = externalTrackingModule.getTrackingNumbers(
                orderInfos.stream().map(OrderInfo::orderCode).toList()
        );

        // 운송 도메인
        transportService.assignVehicle(
                request.vehicleId(),
                orderInfos,
                trackingMap,     // String
                totalWeight
        );
        // 정산 관련 도메인
    }

    @Transactional
    public TransportCancelResponse cancelAssignment(Long transportId) {

        // 운송 도메인 해체 로직
        String cancelledOrderCode = transportService.cancelAssignment(transportId);

        // 응답 DTO 생성
        return TransportCancelResponse.from(transportId, cancelledOrderCode);
    }
}
