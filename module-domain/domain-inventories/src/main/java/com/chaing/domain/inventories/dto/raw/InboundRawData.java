package com.chaing.domain.inventories.dto.raw;

import java.time.LocalDate;

public interface InboundRawData {
    String getBoxCode();

    Long getProductId();

    String getSerialCode();

    LocalDate getManufactureDate();
}