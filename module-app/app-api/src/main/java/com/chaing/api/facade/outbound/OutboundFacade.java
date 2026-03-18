package com.chaing.api.facade.outbound;

import com.chaing.api.dto.outbound.request.OutboundUpdateRequest;
import com.chaing.api.dto.outbound.response.OutboundBoxSummaryResponse;
import com.chaing.api.dto.outbound.response.OutboundItemResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.inventories.dto.info.OutboundGetBoxInfo;
import com.chaing.domain.inventories.dto.info.OutboundGetItemsInfo;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.service.InventoryService;
import com.chaing.domain.inventories.service.OutboundService;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import com.chaing.domain.transports.service.InternalTransportService;
import com.chaing.external.transport.ExternalTransportService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Getter
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Validated
public class OutboundFacade {

    private final OutboundService outboundService;
    private final ProductService productService;
    private final FranchiseOrderService orderService;
    private final BusinessUnitService franchiseServiceImpl;
    private final InventoryService inventoryServiceImpl;
    private final InternalTransportService transportServiceImpl;
    // private final ExternalTransportService externalTransportModule;

    // 재고 상태 변경
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateOutboundStatus(@Valid List<String> request, LogType currentStatus) {
        outboundService.updateStatus(request, currentStatus);

        /*
         * System.out.println(currentStatus);
         * 
         * if(currentStatus == LogType.PICKING) {
         * // 출고 상태로 변경된 재고들의 orderCode 리스트 추출
         * List<Long> orderIds = inventoryServiceImpl.getOrderInfo(selectedList);
         * Map<Long, String> orderInfos =
         * orderService.getAllOrderCodeByOrderIds(orderIds);
         * List<String> orderCodes = orderInfos.values().stream().toList();
         * 
         * // Transit 내 배송중으로 상태 변경
         * transportServiceImpl.updateDeliveryStatus(orderCodes);
         * 
         * // 외부 운송 모듈 호출
         * externalTransportModule.scheduleDeliveryCompletion(orderCodes);
         * }
         */
    }

    // 박스 할당
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void assignBoxToInventories(String boxCode, Long orderItemId, List<String> serialCodes) {

        Long orderId = orderService.getOrderIdByOrderItemId(orderItemId);

        outboundService.assignBox(boxCode, orderId, orderItemId, serialCodes);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void cancelOutbound(String boxCode, List<String> serialCodes) {
        outboundService.cancelOutbound(boxCode, serialCodes);
    }

    // 공장 출고 박스 목록 조회
    public List<OutboundBoxSummaryResponse> getPendingBoxes() {
        List<OutboundGetBoxInfo> getBoxInfos = outboundService.getBoxInfos();

        List<Long> productIds = getBoxInfos.stream().map(OutboundGetBoxInfo::productId).distinct().toList();
        List<Long> orderIds = getBoxInfos.stream().map(OutboundGetBoxInfo::orderId).distinct().toList();

        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        // 안전하게 Map 만들기 (중복 Key 방지)
        Map<Long, FranchiseOrderForTransitResponse> orderMap = orderService.getOrdersForOutbound(orderIds)
                .stream().collect(Collectors.toMap(
                        FranchiseOrderForTransitResponse::orderId,
                        o -> o,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

        Map<String, OutboundBoxSummaryResponse> distinctBoxes = new HashMap<>();

        for (OutboundGetBoxInfo box : getBoxInfos) {
            // 이미 담은 박스면 패스
            if (distinctBoxes.containsKey(box.boxCode()))
                continue;

            ProductInfo product = productMap.get(box.productId());
            FranchiseOrderForTransitResponse order = orderMap.get(box.orderId());

            // 주문 정보가 없을 때를 대비한 안전코드
            String orderCode = (order != null) ? order.orderCode() : "주문 정보 없음";
            String franchiseName = "가맹점 정보 없음";

            if (order != null) {
                franchiseName = franchiseServiceImpl.getById(order.franchiseId()).name();
            }

            if (product == null)
                throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);

            distinctBoxes.put(box.boxCode(), OutboundBoxSummaryResponse.of(
                    box.boxCode(),
                    orderCode,
                    product.productName(),
                    product.productCode(),
                    franchiseName,
                    box.countItem()));
        }

        return new ArrayList<>(distinctBoxes.values());
    }

    public List<OutboundItemResponse> getPendingItems(String boxCode) {
        List<OutboundGetItemsInfo> itemInfos = outboundService.getItemsInfo(boxCode);

        List<Long> productIds = itemInfos.stream()
                .map(OutboundGetItemsInfo::productId)
                .distinct()
                .toList();

        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        return itemInfos.stream()
                .map(box -> {
                    ProductInfo product = productMap.get(box.productId());
                    if (product == null) {
                        throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
                    }
                    return OutboundItemResponse.of(
                            box.serialCode(),
                            box.productId(),
                            product.productName(),
                            box.manufactureDate(),
                            box.isPicking());
                })
                .toList();
    }
}
