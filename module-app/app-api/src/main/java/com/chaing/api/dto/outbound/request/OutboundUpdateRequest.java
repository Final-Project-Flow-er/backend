package com.chaing.api.dto.outbound.request;

import com.chaing.domain.inventories.exception.InventoriesErrorCode;
import com.chaing.domain.inventories.exception.InventoriesException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OutboundUpdateRequest(
        @NotBlank String serialCode,
        @NotNull Long productId,
        @NotNull LocalDate manufactureDate
) {
    public List<String> toSerialCodeList() {
        return List.of(this.serialCode);
    }
}
