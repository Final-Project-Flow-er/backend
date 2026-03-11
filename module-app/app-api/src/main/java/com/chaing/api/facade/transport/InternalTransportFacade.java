package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.api.dto.transport.internal.response.TransportCancelResponse;
import com.chaing.api.dto.transport.internal.response.UnassignedOrderResponse;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.transports.dto.DeliveryFeeInfo;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.transports.dto.response.AvailableVehicleInfo;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.service.InternalTransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportFacade {

    private final InternalTransportService transportService;
    private final FranchiseOrderService franchiseOrderService;
    private final ProductService productService;
    private final BusinessUnitService franchiseServiceImpl;

    // 운송 가능 차량 리스트 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {
        List<AvailableVehicleInfo> domainResponses = transportService.getAvailableVehicle();

        // API용 DTO로 변환
        return domainResponses.stream()
                .map(res -> {
                    long safeMaxLoad = res.maxLoad() == null ? 0L : Math.max(0L, res.maxLoad());
                    long safeCurrentLoad = res.currentWeight() == null ? 0L : Math.max(0L, res.currentWeight());
                    long safeAvailableLoad = Math.max(0L, safeMaxLoad - safeCurrentLoad);
                    return new AvailableVehicleResponse(
                            res.vehicleId(),
                            res.vehicleNumber(),
                            safeMaxLoad,
                            safeCurrentLoad,
                            safeAvailableLoad
                    );
                })
                .toList();
    }

    // 차량 배정
    @Transactional
    public void assignVehicle(VehicleAssignmentRequest request) {

        // 발주 도메인
        // 발주 Id, 중량 정보 받아오기
        List<FranchiseOrderForTransitResponse> orders = franchiseOrderService.getOrdersForTransit(request.orderIds());

        // 상품 Id 추출
        List<OrderInfo> orderInfos = getOrderInfos(orders);


        // 선택된 발주의 총 무게 계산
        Long totalWeight = orderInfos.stream()
                .mapToLong(OrderInfo::weight)
                .sum();

        // 외부 운송 모듈
        // 송장 번호 가져오기
        Map<String, String> trackingMap = Map.of(
                "SE0320260207001", "TRACK-12345",
                "ORD002", "TRACK-67890"
        );
                /* 외부 운송 모듈 구현 전 임시 값으로 대체
                externalTrackingModule.getTrackingNumbers(
                orderInfos.stream().map(OrderInfo::orderCode).toList()
        );*/

        // 운송 도메인
        transportService.assignVehicle(
                request.vehicleId(),
                orderInfos,
                trackingMap,     // String
                totalWeight
        );

        // 정산 관련 도메인
        List<DeliveryFeeInfo> deliveryFees = transportService.calculateDeliveryFee(orderInfos, request.vehicleId());

    }

    @Transactional
    public TransportCancelResponse cancelAssignment(Long transportId) {

        // 운송 도메인 해체 로직
        String cancelledOrderCode = transportService.cancelAssignment(transportId);

        // 응답 DTO 생성
        return TransportCancelResponse.from(transportId, cancelledOrderCode);
    }

    public List<UnassignedOrderResponse> getUnassignedOrders() {

        // 차량 미배정 발주 조회(franchiseOrderStatus = PARTIAL, ACCEPTED)
        List<FranchiseOrderForTransitResponse> unassignedOrderList = franchiseOrderService.getOrdersForAssignVehicle();

        List<OrderInfo> orderInfos = getOrderInfos(unassignedOrderList);

        // franchiseName, address, 대표 이름(수령인) 가져오기 (franchise)에서
        List<Long> franchiseIds = orderInfos.stream()
                .map(OrderInfo::franchiseId)
                .toList();

        Map<Long, BusinessUnitInternal> franchiseMap = franchiseIds.stream()
                .map(franchiseServiceImpl::getById)
                .collect(Collectors.toMap(
                        BusinessUnitInternal::id,
                        info -> info,
                        (existing, replacement) -> existing
                ));

        return orderInfos.stream()
                .map(orderInfo -> {
                    BusinessUnitInternal franchiseInfo = franchiseMap.get(orderInfo.franchiseId());
                    if (franchiseInfo == null) {
                        throw new BusinessUnitException(BusinessUnitErrorCode.BUSINESS_UNIT_NOT_FOUND);
                    }
                    return UnassignedOrderResponse.from(orderInfo, franchiseInfo);
                })
                .toList();
    }

    public List<OrderInfo> getOrderInfos(List<FranchiseOrderForTransitResponse> orders) {

        List<Long> productIds = orders.stream()
                .flatMap(order -> order.items().stream())
                .map(FranchiseOrderForTransitResponse.OrderItemForTransit::productId)
                .distinct()
                .toList();

        // 상품 무게와 정보 조회
        Map<Long, Integer> weightMap = productService.getWeightsByProductIds(productIds);

        // 발주 정보 dto 타입 변환(가공)
        return orders.stream()
                .map(order -> {
                    long orderWeight = order.items().stream()
                            .mapToLong(item -> {
                                Integer weight = weightMap.get(item.productId());
                                if (weight == null) {
                                    throw new TransportException(TransportErrorCode.TRANSPORT_WEIGHT_IS_NOT_VALID);
                                }
                                return (long) weight * item.quantity();
                            })
                            .sum();
                    return new OrderInfo(order.orderId(), order.orderCode(), orderWeight, order.franchiseId(), order.orderCreatedAt(), order.deliveryDate());
                })
                .toList();
    }

}
