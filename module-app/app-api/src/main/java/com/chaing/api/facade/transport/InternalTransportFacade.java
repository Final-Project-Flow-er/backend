package com.chaing.api.facade.transport;

import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.api.dto.transport.internal.response.TransportCancelResponse;
import com.chaing.api.dto.transport.internal.response.TransportLogResponse;
import com.chaing.api.dto.transport.internal.response.UnassignedOrderResponse;
import com.chaing.api.dto.transport.internal.response.UnassignedReturnResponse;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.exception.BusinessUnitErrorCode;
import com.chaing.domain.businessunits.exception.BusinessUnitException;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.returns.dto.command.HQReturnCommand;
import com.chaing.domain.returns.service.FranchiseReturnService;
import com.chaing.domain.transports.dto.DeliveryFeeInfo;
import com.chaing.domain.transports.dto.OrderInfo;
import com.chaing.domain.returns.dto.command.FranchiseReturnCommandForTransit;
import com.chaing.domain.transports.dto.info.TransportLogInfo;
import com.chaing.domain.transports.dto.response.AvailableVehicleInfo;
import com.chaing.domain.transports.exception.TransportErrorCode;
import com.chaing.domain.transports.exception.TransportException;
import com.chaing.domain.transports.service.InternalTransportService;
import com.chaing.external.transport.ExternalTransportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chaing.domain.returns.enums.ReturnStatus.ACCEPTED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalTransportFacade {

    private final InternalTransportService transportService;
    private final FranchiseOrderService franchiseOrderService;
    private final ProductService productService;
    private final BusinessUnitService franchiseServiceImpl;
    private final FranchiseReturnService franchiseReturnService;
    private final ExternalTransportService externalTrackingModule;

    // 운송 가능 차량 리스트 조회
    public List<AvailableVehicleResponse> getAvailableVehicle() {
        List<AvailableVehicleInfo> domainResponses = transportService.getAvailableVehicle();

        // API용 DTO로 변환
        return domainResponses.stream()
                .map(res -> {
                    long safeMaxLoad = res.maxLoad() == null ? 0L : Math.max(0L, res.maxLoad());
                    long safeCurrentLoad = res.currentLoad() == null ? 0L : Math.max(0L, res.currentLoad());
                    long safeAvailableLoad = Math.max(0L, safeMaxLoad - safeCurrentLoad);
                    return new AvailableVehicleResponse(
                            res.transportName(),
                            res.driverName(),
                            res.driverPhoneNumber(),
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
        List<FranchiseOrderForTransitResponse> orders = franchiseOrderService.getOrdersForTransit(request.selectedIds());

        // 상품 Id 추출
        List<OrderInfo> orderInfos = getOrderInfos(orders);


        // 선택된 발주의 총 무게 계산
        Long totalWeight = orderInfos.stream()
                .mapToLong(OrderInfo::weight)
                .sum();

        // 외부 운송 모듈
        // 송장 번호 가져오기
        Map<String, String> trackingMap = externalTrackingModule
                .getTrackingNumbers(orderInfos.stream()
                        .map(OrderInfo::orderCode)
                        .toList());

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

    public List<AvailableVehicleResponse> getVehicleForReturn() {
        List<AvailableVehicleInfo> domainResponses = transportService.getAllAvailableVehicle();

        return domainResponses.stream()
                .map(AvailableVehicleResponse::from)
                .toList();
    }

    public List<UnassignedReturnResponse> getUnassignedReturns() {

        Map<Long, HQReturnCommand> returnCommandMap = franchiseReturnService.getAllReturnByStatus(ACCEPTED);

        Map<Long, BusinessUnitInternal> franchiseMap = returnCommandMap.values().stream()
                .map(command -> franchiseServiceImpl.getById(command.franchiseId()))
                .collect(Collectors.toMap(
                        BusinessUnitInternal::id,
                        info -> info,
                        (existing, replacement) -> existing
                ));

        return returnCommandMap.values().stream()
                .map(returnInfo -> {
                    BusinessUnitInternal franchiseInfo = franchiseMap.get(returnInfo.franchiseId());

                    return UnassignedReturnResponse.from(returnInfo, franchiseInfo);
                })
                .collect(Collectors.toList());
    }

    // 반품 차량 배정
    @Transactional
    public void assignVehicleReturns(@Valid VehicleAssignmentRequest request) {

        List<FranchiseReturnCommandForTransit> returns = franchiseReturnService.getReturnForTransit(request.selectedIds());

        List<Long> orderIds = returns.stream().map(FranchiseReturnCommandForTransit::franchiseOrderId).toList();

        List<FranchiseOrderForTransitResponse> orders = franchiseOrderService.getOrdersForTransit(orderIds);

        List<OrderInfo> orderInfos = getOrderInfos(orders);

        // 선택된 발주의 총 무게 계산
        Long totalWeight = orderInfos.stream()
                .mapToLong(OrderInfo::weight)
                .sum();

        List<String> returnCodes = returns.stream()
                .map(FranchiseReturnCommandForTransit::returnCode)
                .toList();

        // 외부 운송 모듈
        // 송장 번호 가져오기
        Map<String, String> trackingMap =
                externalTrackingModule.getTrackingNumbers(
                orderInfos.stream().map(OrderInfo::orderCode).toList()
        );

        transportService.assignVehicleReturn(
                request.vehicleId(),
                orderInfos,
                trackingMap,     // String
                totalWeight,
                returnCodes
        );
    }

    public void updateDeliverStatus(@NotEmpty List<String> orderCodes) {

        boolean isPendingOrders = transportService.filterPendingOrders(orderCodes);

        transportService.updateDeliveryStatus(orderCodes);
        if (isPendingOrders) {
            externalTrackingModule.scheduleDeliveryCompletion(orderCodes);
        }
    }

    public List<TransportLogResponse> getTransportLog() {
        List<TransportLogInfo> logInfos = transportService.getTransportLog();
        List<Long> franchiseIds = logInfos.stream().map(TransportLogInfo::franchiseId).toList();
        Map<Long, String> franchiseNameMap = franchiseServiceImpl.getNamesByIds(franchiseIds);

        return logInfos.stream()
                .map(log -> {
                    String franchiseName = franchiseNameMap.get(log.franchiseId());

                    return TransportLogResponse.from(log, franchiseName);
                })
                .toList();
    }
}
