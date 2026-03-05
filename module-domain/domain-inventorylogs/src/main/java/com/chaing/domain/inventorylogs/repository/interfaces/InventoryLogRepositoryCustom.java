package com.chaing.domain.inventorylogs.repository.interfaces;

import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;

import java.util.List;

public interface InventoryLogRepositoryCustom {
    InventoryLogListResponse findReturnInboundLogs(LogRequest request);
    InventoryLogListResponse findReturnOutboundLogs(LogRequest request);
    InventoryLogListResponse findDisposalLogs(LogRequest request);
    FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId, FranchiseLogRequest request);
    FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request);
    InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request);

    List<FranchiseProductSalesResponse> getProductSales(List<Long> franchiseId, List<Long> productIds);
}