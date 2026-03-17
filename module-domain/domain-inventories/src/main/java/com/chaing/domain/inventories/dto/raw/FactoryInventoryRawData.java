package com.chaing.domain.inventories.dto.raw;

import com.chaing.core.enums.LogType;

import java.time.LocalDate;

public record FactoryInventoryRawData(
        Long productId,
        String serialCode,
        LocalDate manufactureDate,
        LogType status,
        Long orderId,
        Long orderItemId
) implements InboundRawData {
    @Override
    public String getBoxCode() {return "";}

    @Override
    public Long getProductId() {return productId;}

    @Override
    public String getSerialCode() {return serialCode;}

    @Override
    public LocalDate getManufactureDate() {return manufactureDate;}

    @Override
    public Long getOrderId() {return orderId;}
}
