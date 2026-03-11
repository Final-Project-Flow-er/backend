package com.chaing.domain.inventories.dto.raw;

import com.chaing.core.enums.LogType;

import java.time.LocalDate;

public record FranchiseInventoryRawData(
        String boxCode,
        Long productId,
        String serialCode,
        LocalDate manufactureDate,
        Long franchiseId,
        Long orderId,
        LogType status
) implements InboundRawData
{
    @Override
    public String getBoxCode() {return boxCode;}

    @Override
    public Long getProductId() {return productId;}

    @Override
    public String getSerialCode() {return serialCode;}

    @Override
    public LocalDate getManufactureDate() {return manufactureDate;}

    @Override
    public Long getOrderId() {return orderId;}
}
