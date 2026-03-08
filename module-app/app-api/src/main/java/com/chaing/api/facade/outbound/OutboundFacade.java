package com.chaing.api.facade.outbound;

import com.chaing.api.dto.outbound.request.OutboundUpdateRequest;
import com.chaing.api.dto.outbound.response.OutboundBoxSummaryResponse;
import com.chaing.api.dto.outbound.response.OutboundItemResponse;
import com.chaing.core.dto.info.ProductInfo;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.info.OutboundGetBoxInfo;
import com.chaing.domain.inventories.dto.info.OutboundGetItemsInfo;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import com.chaing.domain.inventories.service.OutboundService;
import com.chaing.domain.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Service
@Getter
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Validated
public class OutboundFacade {

    private final OutboundService outboundService;
    private final ProductService productService;

    // 재고 상태 변경
    public void updateOutboundStatus(@Valid OutboundUpdateRequest request, LogType currentStatus) {
        List<String> selectedList = request.serialCodes();
        outboundService.updateStatus(selectedList, currentStatus);
    }

    // 박스 할당
    public void assignBoxToInventories(String boxCode, List<String> serialCodes) {
        outboundService.assignBox(boxCode, serialCodes);
    }

    public void cancelOutbound(String boxCode, List<String> serialCodes) {
        outboundService.cancelOutbound(boxCode, serialCodes);
    }

    public List<OutboundBoxSummaryResponse> getPendingBoxes() {
        // 공장 재고 내에서 productId 조회
        List<OutboundGetBoxInfo> getBoxInfos = outboundService.getBoxInfos();

        List<Long> productIds = getBoxInfos.stream()
                .map(OutboundGetBoxInfo::productId)
                .distinct()
                .toList();

        // productId로 productName, productCode 조회
        Map<Long, ProductInfo> productMap = productService.getProductInfos(productIds);

        return getBoxInfos.stream()
                .map(box ->{
                    ProductInfo product =  productMap.get(box.productId());
                    if(product == null){
                        throw new InventoriesException(InventoriesErrorCode.INVENTORIES_IS_NULL);
                    }
                    return OutboundBoxSummaryResponse.of(
                            box.boxCode(),
                            product.productName(),
                            product.productCode()
                    );
                })
                .distinct()
                .toList();
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
                            box.manufactureDate()
                    );
                })
                .toList();
    }
}
