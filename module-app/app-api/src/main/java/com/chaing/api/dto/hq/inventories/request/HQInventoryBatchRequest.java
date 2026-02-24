package com.chaing.api.dto.hq.inventories.request;

import java.util.List;

public record HQInventoryBatchRequest(
        List<HQInventoryBoxRequest> boxes
) {
}
