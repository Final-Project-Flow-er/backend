package com.chaing.api.dto.franchise.sales.response;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.inventories.dto.request.InventoryRequest;
import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ScannedForSaleResponse(
        @NotNull
        Integer totalQuantity,

        @NotNull
        @Min(1)
        BigDecimal totalAmount,

        @NotNull
        List<ScannedItemForSaleResponse> requestList
) {
    public static ScannedForSaleResponse create(List<InventoryRequest> scannedItems, Map<Long, ProductInfo> productInfos){

        // 개별 상품 리스트
        List<ScannedItemForSaleResponse> requestList = scannedItems.stream()
                .map(item -> {
                    ProductInfo info = productInfos.get(item.productId());
                    if(info == null) {
                        throw new InventoriesException(InventoriesErrorCode.PRODUCT_NOT_FOUND);
                    }
                    return ScannedItemForSaleResponse.create(
                            item.productId(),
                            info.productCode(),
                            info.productName(),
                            info.retailPrice(),
                            item.serialCode()
                    );
                })
                .toList();

        int totalQuantity = requestList.size();

        BigDecimal totalAmount = requestList.stream()
                .map(ScannedItemForSaleResponse::unitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ScannedForSaleResponse(totalQuantity, totalAmount, requestList);
    }
}
