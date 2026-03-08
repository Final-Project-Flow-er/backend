package com.chaing.api.facade.factory;

import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FactoryInventoryLogFacade {
    private final InventoryLogService inventoryLogService;

    public InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
        return inventoryLogService.findFactoryInventoryLogs(factoryId, request);
    }
}
