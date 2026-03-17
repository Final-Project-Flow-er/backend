package com.chaing.domain.inventorylogs.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record InventoryLogListResponse(
                List<InventoryLogResponse> inventoryLogResponses,
                long totalElements,
                int totalPages) {
}
