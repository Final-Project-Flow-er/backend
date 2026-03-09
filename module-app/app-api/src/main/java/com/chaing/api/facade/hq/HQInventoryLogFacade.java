package com.chaing.api.facade.hq;


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


//그냥 엔티티 Response 사용하자..Request도..
@RequiredArgsConstructor
@Service
@Transactional(readOnly=true)
public class HQInventoryLogFacade {
    private final InventoryLogService inventoryLogService;


    public InventoryLogListResponse findReturnInboundLogs(LogRequest request) {
        return inventoryLogService.findReturnInboundLogs(request);
    }

    public InventoryLogListResponse findReturnOutboundLogs(LogRequest request) {
        return inventoryLogService.findReturnOutboundLogs(request);
    }


    public InventoryLogListResponse findDisposalLogs(LogRequest request) {
        return inventoryLogService.findDisposalLogs(request);
    }


    // 얜 Request 사용함, 저장될 때는 상품이름이 같이 저징이 되지만 아마 로그 기록할 때 상품참조할 듯
    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId, FranchiseLogRequest request) {
        return inventoryLogService.findFranchiseInboundOutboundLogs(franchiseId, request);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request) {
        return inventoryLogService.findFranchiseSalesRefundLogs(franchiseId, request);
    }

    public FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
        return inventoryLogService.findFactoryInventoryLogs(factoryId, request);
    }
}
