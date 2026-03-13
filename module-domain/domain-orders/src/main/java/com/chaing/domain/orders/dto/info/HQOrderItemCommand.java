package com.chaing.domain.orders.dto.info;

import com.chaing.core.dto.info.ProductInfo;
import com.chaing.domain.orders.entity.HeadOfficeOrder;
import com.chaing.domain.orders.entity.HeadOfficeOrderItem;
import com.chaing.domain.orders.exception.HQOrderErrorCode;
import com.chaing.domain.orders.exception.HQOrderException;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record HQOrderItemCommand(
        Long orderId,

        Long orderItemId,

        Long productId,

        String productCode,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice
) {
    public static HQOrderItemCommand of(HeadOfficeOrderItem item, Map<Long, ProductInfo> productInfoByProductId) {
        Long productId = item.getProductId();
        ProductInfo info = productInfoByProductId.get(productId);

        if (info == null) {
            throw new HQOrderException(HQOrderErrorCode.PRODUCT_NOT_FOUND);
        }

        return HQOrderItemCommand.builder()
                .orderId(item.getHeadOfficeOrder().getHeadOfficeOrderId())
                .orderItemId(item.getHeadOfficeOrderItemId())
                .productId(productId)
                .productCode(info.productCode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public static List<HQOrderItemCommand> ofList(List<HeadOfficeOrderItem> orderItems, Map<Long, ProductInfo> productInfoByProductId) {
        return orderItems.stream()
                .map(item -> HQOrderItemCommand.of(item, productInfoByProductId))
                .toList();
    }

    public static HQOrderItemCommand from(HeadOfficeOrderItem item) {
        return HQOrderItemCommand.builder()
                .orderId(item.getHeadOfficeOrder().getHeadOfficeOrderId())
                .orderItemId(item.getHeadOfficeOrderItemId())
                .productId(item.getProductId())
                .productCode(null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public static List<HQOrderItemCommand> from(List<HeadOfficeOrderItem> items) {
        return items.stream()
                .map(HQOrderItemCommand::from)
                .toList();
    }
}
