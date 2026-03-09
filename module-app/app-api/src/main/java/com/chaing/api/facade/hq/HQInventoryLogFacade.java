package com.chaing.api.facade.hq;

import org.springframework.data.domain.Pageable;

import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.service.InventoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class HQInventoryLogFacade {
    private final InventoryLogService inventoryLogService;

    public InventoryLogListResponse findReturnInboundLogs(LogRequest request, Pageable pageable) {
        return inventoryLogService.findReturnInboundLogs(request, pageable);
    }

    public InventoryLogListResponse findReturnOutboundLogs(LogRequest request, Pageable pageable) {
        return inventoryLogService.findReturnOutboundLogs(request, pageable);
    }

    public InventoryLogListResponse findDisposalLogs(LogRequest request, Pageable pageable) {
        return inventoryLogService.findDisposalLogs(request, pageable);
    }

    // 얜 Request 사용함, 저장될 때는 상품이름이 같이 저징이 되지만 아마 로그 기록할 때 상품참조할 듯
    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        return inventoryLogService.findFranchiseInboundOutboundLogs(franchiseId, request, pageable);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request,
            Pageable pageable) {
        return inventoryLogService.findFranchiseSalesRefundLogs(franchiseId, request, pageable);
    }

    public FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request,
            Pageable pageable) {
        return inventoryLogService.findFactoryInventoryLogs(factoryId, request, pageable);
    }
}
