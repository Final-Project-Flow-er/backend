package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;

import java.util.List;
import java.util.Map;

public interface FactoryInventoryRepositoryCustom {
    Map<Long, InventoryProductInfoResponse> getStock(List<Long> products, String status);

    List<HQInventoryBatchResponse> getBatches(Long productId);

    List<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request);

    void updateStatus(List<String> serialCode, LogType status);

    void deleteFactoryInventory(List<String> serialCode);

    List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId);

    long updateExpiredStatus(java.time.LocalDate expirationDate);

    List<com.chaing.domain.inventories.dto.response.SafetyStockResponse> getLowStockAlerts(String locationType,
            Long locationId);
}
