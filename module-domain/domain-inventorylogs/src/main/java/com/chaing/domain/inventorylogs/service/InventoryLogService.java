package com.chaing.domain.inventorylogs.service;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.InventoryLogCreateRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
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

    public InventoryLogListResponse findReturnInboundLogs(LogRequest request) {
        return inventoryLogRepository.findReturnInboundLogs(request);
    }

    public InventoryLogListResponse findReturnOutboundLogs(LogRequest logRequest) {
        return inventoryLogRepository.findReturnOutboundLogs(logRequest);
    }

    public InventoryLogListResponse findDisposalLogs(LogRequest logRequest) {
        return inventoryLogRepository.findDisposalLogs(logRequest);
    }

    public FranchiseInventoryLogListResponse findFranchiseInboundOutboundLogs(Long franchiseId,
            FranchiseLogRequest request) {
        return inventoryLogRepository.findFranchiseInboundOutboundLogs(franchiseId, request);
    }

    public FranchiseInventoryLogListResponse findFranchiseSalesRefundLogs(Long franchiseId,
            FranchiseLogRequest request) {
        return inventoryLogRepository.findFranchiseSalesRefundLogs(franchiseId, request);
    }

    public InventoryLogListResponse findFactoryInventoryLogs(Long factoryId, FactoryLogRequest request) {
        return inventoryLogRepository.findFactoryInventoryLogs(factoryId, request);
    }

    public List<ActorProductSalesResponse> getProductSales(List<Long> actorIds, List<Long> productIds,
            ActorType actorType, LogType logType) {
        return inventoryLogRepository.getProductSales(actorIds, productIds, actorType, logType);
    }

    public void recordInventoryLog(List<InventoryLogCreateRequest> logs) {
        List<InventoryLog> entities = logs.stream()
                .map(this::toEntity)
                .toList();

        inventoryLogRepository.saveAll(entities);
    }

    private InventoryLog toEntity(InventoryLogCreateRequest request) {
        return InventoryLog.builder()
                .productId(request.productId())
                .productName(request.productName())
                .boxCode(request.boxCode())
                .transactionCode(request.transactionCode())
                .logType(request.logType()) // 받을 때 스캔하면 INBOUND, 보낼때 스캔하면 OUTBOUND
                .quantity(request.quantity())
                .supplyPrice(request.supplyPrice())
                .price(request.price())
                .fromLocationType(request.fromLocationType())
                .fromLocationId(request.fromLocationId())
                .toLocationType(request.toLocationType())
                .toLocationId(request.toLocationId())
                .actorType(request.actorType())
                .actorId(request.actorId())
                .build();
    }
}
