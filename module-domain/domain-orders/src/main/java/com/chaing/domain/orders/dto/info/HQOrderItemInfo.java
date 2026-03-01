package com.chaing.domain.orders.dto.info;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record HQOrderItemInfo(
        Long productId,

        String productCode,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {
    public static HQOrderItemInfo of(HeadOfficeOrderItem item, Map<Long, ProductInfo> productInfoByProductId) {
        Long productId = item.getProductId();
        ProductInfo info = productInfoByProductId.get(productId);

        return HQOrderItemInfo.builder()
                .productId(productId)
                .productCode(info.productCode())
                .productName(info.productName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
