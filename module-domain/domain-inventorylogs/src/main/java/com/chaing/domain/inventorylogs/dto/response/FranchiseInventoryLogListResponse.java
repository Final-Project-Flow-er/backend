package com.chaing.domain.inventorylogs.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FranchiseInventoryLogListResponse (
        List<FranchiseInventoryLogResponse> franchiseInventoryLogResponseList
){
}
