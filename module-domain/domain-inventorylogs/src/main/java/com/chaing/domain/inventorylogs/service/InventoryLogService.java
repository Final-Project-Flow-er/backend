package com.chaing.domain.inventorylogs.service;

import org.springframework.data.domain.Pageable;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.ActorProductSalesResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import com.chaing.domain.inventorylogs.entity.InventoryLog;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;

    public InventoryLogListResponse findReturnInboundLogs(Long hqId, LogRequest request, Pageable pageable) {
        return inventoryLogRepository.findReturnInboundLogs(hqId, request, pageable);
    }

    public InventoryLogListResponse findReturnOutboundLogs(Long hqId, LogRequest logRequest, Pageable pageable) {
        return inventoryLogRepository.findReturnOutboundLogs(hqId, logRequest, pageable);
    }

    public InventoryLogListResponse findDisposalLogs(Long hqId, LogRequest logRequest, Pageable pageable) {
        return inventoryLogRepository.findDisposalLogs(hqId, logRequest, pageable);
    }

    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        return inventoryLogRepository.findFranchiseInboundOutboundLogs(franchiseId, request, pageable);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId,
            FranchiseLogRequest request, Pageable pageable) {
        return inventoryLogRepository.findFranchiseSalesRefundLogs(franchiseId, request, pageable);
    }

    public FactoryInventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request,
            Pageable pageable) {
        return inventoryLogRepository.findFactoryInventoryLogs(factoryId, request, pageable);
    }

    public List<ActorProductSalesResponse> getProductSales(List<Long> actorIds, List<Long> productIds,
            ActorType actorType, LogType logType) {
        return inventoryLogRepository.getProductSales(actorIds, productIds, actorType, logType);
    }

    public void recordInventoryLog(List<InventoryLogCreateRequest> logs) {
        List<InventoryLog> entities = logs.stream()
                .filter(request -> !inventoryLogRepository
                        .existsByTransactionCodeAndBoxCodeAndLogTypeAndActorTypeAndActorIdAndDeletedAtIsNull(
                                request.transactionCode(),
                                request.boxCode(),
                                request.logType(),
                                request.actorType(),
                                request.actorId()))
                .map(this::toEntity)
                .toList();

        if (!entities.isEmpty()) {
            inventoryLogRepository.saveAll(entities);
        }
    }

    private InventoryLog toEntity(InventoryLogCreateRequest request) {
        return InventoryLog.builder()
                .productId(request.productId())
                .productName(request.productName())
                .boxCode(request.boxCode())
                .transactionCode(request.transactionCode())
                .logType(request.logType()) // 받을 때 스캔하면 INBOUND, 보낼때 스캔하면 OUTBOUND
                .quantity(request.quantity())
                .fromLocationType(request.fromLocationType())
                .fromLocationId(request.fromLocationId())
                .toLocationType(request.toLocationType())
                .toLocationId(request.toLocationId())
                .actorType(request.actorType())
                .actorId(request.actorId())
                .build();
    }

    public List<BoxCodeResponse> findBoxCodesByTransactionCode(String transactionCode) {
        return inventoryLogRepository.findBoxCodesByTransactionCode(transactionCode);
    }
}
