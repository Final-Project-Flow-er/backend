package com.chaing.api.facade.factory;


import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryInventoryLogFacade {
    private final InventoryLogService inventoryLogService;

    public InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
        return inventoryLogService.findFactoryInventoryLogs(factoryId, request);
    }
}
