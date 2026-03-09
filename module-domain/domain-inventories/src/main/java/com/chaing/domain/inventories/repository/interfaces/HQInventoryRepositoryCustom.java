package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;

import java.util.List;

public interface HQInventoryRepositoryCustom {
    void deleteHQInventory(List<String> serialCode);

    long updateExpiredStatus(java.time.LocalDate expirationDate);
}
