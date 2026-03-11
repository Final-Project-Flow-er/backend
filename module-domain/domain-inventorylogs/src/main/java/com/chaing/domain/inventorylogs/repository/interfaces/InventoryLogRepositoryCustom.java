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

import org.springframework.data.domain.Pageable;

public interface InventoryLogRepositoryCustom {
    InventoryLogListResponse findReturnInboundLogs(Long hqId, LogRequest request, Pageable pageable);

    InventoryLogListResponse findReturnOutboundLogs(Long hqId, LogRequest request, Pageable pageable);

    InventoryLogListResponse findDisposalLogs(Long hqId, LogRequest request, Pageable pageable);

    FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId, FranchiseLogRequest request,
            Pageable pageable);

    FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId, FranchiseLogRequest request,
            Pageable pageable);

    FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request,
            Pageable pageable);

    List<ActorProductSalesResponse> getProductSales(List<Long> actorId, List<Long> productIds, ActorType actorType,
            LogType logType);

    List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode);
}