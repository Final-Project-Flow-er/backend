package com.chaing.domain.inventorylogs.repository.interfaces;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.ActorProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.enums.ActorType;

import java.util.List;

public interface InventoryLogRepositoryCustom {
    InventoryLogListResponse findReturnInboundLogs(LogRequest request);

    InventoryLogListResponse findReturnOutboundLogs(LogRequest request);

    InventoryLogListResponse findDisposalLogs(LogRequest request);

    FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId, FranchiseLogRequest request);

    FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request);

    FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request);

    List<ActorProductSalesResponse> getProductSales(List<Long> actorId, List<Long> productIds, ActorType actorType,
            LogType logType);

    List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode);
}