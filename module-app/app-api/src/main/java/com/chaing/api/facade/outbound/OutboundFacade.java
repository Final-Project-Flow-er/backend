package com.chaing.api.facade.outbound;

import com.chaing.api.dto.outbound.request.OutboundUpdateRequest;
import com.chaing.api.dto.outbound.response.OutboundBoxSummaryResponse;
import com.chaing.api.dto.outbound.response.OutboundItemResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.businessunits.dto.internal.BusinessUnitInternal;
import com.chaing.domain.businessunits.service.BusinessUnitService;
import com.chaing.domain.inventories.dto.info.OutboundGetBoxInfo;
import com.chaing.domain.inventories.dto.info.OutboundGetItemsInfo;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.service.OutboundService;
import com.chaing.domain.orders.dto.response.FranchiseOrderForTransitResponse;
import com.chaing.domain.orders.service.FranchiseOrderService;
import com.chaing.domain.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    private final BusinessUnitService businessUnitService;

    // 재고 상태 변경
    @Transactional
    public void updateOutboundStatus(@Valid OutboundUpdateRequest request, LogType currentStatus) {
        List<String> selectedList = request.serialCodes();
        outboundService.updateStatus(selectedList, currentStatus);
    }

    // 박스 할당
    @Transactional
    public void assignBoxToInventories(String boxCode, List<String> serialCodes) {
        outboundService.assignBox(boxCode, serialCodes);
    }

    @Transactional
    public void cancelOutbound(String boxCode, List<String> serialCodes) {
        outboundService.cancelOutbound(boxCode, serialCodes);
    }

    // 공장 출고 박스 목록 조회
    public List<OutboundBoxSummaryResponse> getPendingBoxes() {
        List<OutboundGetBoxInfo> getBoxInfos = outboundService.getBoxInfos();

        List<Long> productIds = getBoxInfos.stream().map(OutboundGetBoxInfo::productId).distinct().toList();
        List<Long> orderIds = getBoxInfos.stream().map(OutboundGetBoxInfo::orderId).distinct().toList();

        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);
        Map<Long, FranchiseOrderForTransitResponse> orderMap = orderService.getOrdersForTransit(orderIds)
                .stream().collect(Collectors.toMap(FranchiseOrderForTransitResponse::orderId, o -> o));

        Map<String, OutboundBoxSummaryResponse> distinctBoxes = new HashMap<>();

        for (OutboundGetBoxInfo box : getBoxInfos) {
            if (distinctBoxes.containsKey(box.boxCode())) continue;

            ProductInfo product = productMap.get(box.productId());
            if (product == null) throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);

            FranchiseOrderForTransitResponse order = orderMap.get(box.orderId());
            String franchiseName = "";
            if (order != null) {
                franchiseName = businessUnitService.getById(order.franchiseId()).name();
            }

            // Response 객체 생성
            distinctBoxes.put(box.boxCode(), OutboundBoxSummaryResponse.of(
                    box.boxCode(),
                    order.orderCode(),
                    product.productName(),
                    product.productCode(),
                    franchiseName,
                    box.countItem()
            ));
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
                .map(box ->{
                    ProductInfo product =  productMap.get(box.productId());
                    if(product == null){
                        throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
                    }
                    return OutboundItemResponse.of(
                            box.serialCode(),
                            box.productId(),
                            product.productName(),
                            box.manufactureDate(),
                            box.isPicking()
                    );
                })
                .toList();
    }
}
