package com.chaing.domain.inventories.repository.interfaces;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.dto.request.HQInventoryItemsRequest;
import com.chaing.domain.inventories.dto.response.ExpirationBatchResultResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryBatchResponse;
import com.chaing.domain.inventories.dto.response.HQInventoryItemResponse;
import com.chaing.domain.inventories.dto.response.InventoryProductInfoResponse;
import com.chaing.domain.inventories.dto.response.SafetyStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FactoryInventoryRepositoryCustom {
    Map<Long, InventoryProductInfoResponse> getStock(List<Long> products, String status);

    Page<HQInventoryBatchResponse> getBatches(Long productId, Pageable pageable);

    Page<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request, Pageable pageable);

    void updateStatus(List<String> serialCode, LogType status);

    void deleteFactoryInventory(List<String> serialCode);

    List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId);

    long updateExpiredStatus(LocalDate expirationDate);

    List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId);
}
