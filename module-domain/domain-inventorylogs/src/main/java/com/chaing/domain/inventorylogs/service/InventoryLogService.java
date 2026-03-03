package com.chaing.domain.inventorylogs.service;


import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    public InventoryLogListResponse findReturnInboundLogs(LogRequest request) {
        return inventoryLogRepository.findReturnInboundLogs(request);
    }

    public InventoryLogListResponse findReturnOutboundLogs(LogRequest logRequest) {
        return inventoryLogRepository.findReturnOutboundLogs(logRequest);
    }

    public InventoryLogListResponse findDisposalLogs(LogRequest logRequest) {
        return inventoryLogRepository.findDisposalLogs(logRequest);
    }


    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId, FranchiseLogRequest request) {
        return inventoryLogRepository.findFranchiseInboundOutboundLogs(franchiseId, request);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request) {
        return inventoryLogRepository.findFranchiseSalesRefundLogs(franchiseId, request);
    }

    public InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
        return inventoryLogRepository.findFactoryInventoryLogs(factoryId, request);
    }
}
