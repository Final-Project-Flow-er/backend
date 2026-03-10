package com.chaing.api.facade.franchise;

import org.springframework.data.domain.Pageable;

import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FranchiseInventoryLogFacade {
    private final InventoryLogService inventoryLogService;

    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        return inventoryLogService.findFranchiseInboundOutboundLogs(franchiseId, request, pageable);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request,
            Pageable pageable) {
        return inventoryLogService.findFranchiseSalesRefundLogs(franchiseId, request, pageable);
    }
}
