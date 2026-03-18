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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FactoryInventoryRepositoryCustom {
    Map<Long, InventoryProductInfoResponse> getStock(List<Long> products, String status);

    Map<String, Long> countByBoxCodes(List<String> boxCodes);

    Page<HQInventoryBatchResponse> getBatches(Long productId, Pageable pageable);

    Page<HQInventoryItemResponse> getItems(HQInventoryItemsRequest request, Pageable pageable);

    @Modifying
    @Query("UPDATE FactoryInventory fi SET fi.status = :status, fi.version = fi.version + 1 WHERE fi.serialCode IN :serialCode")
    void updateStatus(List<String> serialCode, LogType status);

    void deleteFactoryInventory(List<String> serialCode);

    List<ExpirationBatchResultResponse> getExpirationAlerts(String locationType, Long locationId);

    long updateExpiredStatus(LocalDate expirationDate);

    List<SafetyStockResponse> getLowStockAlerts(String locationType, Long locationId);
}
