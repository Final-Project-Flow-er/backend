package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.FranchiseInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.FranchiseInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;

import java.util.List;
import java.util.Map;

public interface FranchiseInventoryRepositoryCustom {
    Map<Long, InventoryProductInfoResponse> getFranchiseStock(Long franchiseId, List<Long> ids, String status);

    List<FranchiseInventoryBatchResponse> getFranchiseBatches(Long franchiseId, Long productId);

    List<FranchiseInventoryItemResponse> getFranchiseItems(Long franchiseId, FranchiseInventoryItemsRequest request);

    void deleteFranchiseInventory(Long franchiseId, List<String> serialCode);

    void updateFranchiseStatus(Long franchiseId, List<String> serialCode, LogType logType);

    List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId);

    long updateExpiredStatus(java.time.LocalDate expirationDate);

    List<com.chaing.domain.inventories.dto.response.SafetyStockResponse> getLowStockAlerts(String locationType,
            Long locationId);
}
