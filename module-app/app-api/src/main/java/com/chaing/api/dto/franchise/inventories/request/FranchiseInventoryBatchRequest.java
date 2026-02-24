package com.chaing.api.dto.franchise.inventories.request;

import java.util.List;

public record FranchiseInventoryBatchRequest(
        List<FranchiseInventoryBoxRequest> boxes
) {
}
