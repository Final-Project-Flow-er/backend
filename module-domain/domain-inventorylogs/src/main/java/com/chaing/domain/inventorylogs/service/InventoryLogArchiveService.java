package com.chaing.domain.inventorylogs.service;

import com.chaing.domain.inventorylogs.entity.InventoryLog;
import com.chaing.domain.inventorylogs.entity.InventoryLogArchive;
import com.chaing.domain.inventorylogs.repository.InventoryLogArchiveRepository;
import com.chaing.domain.inventorylogs.repository.InventoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryLogArchiveService {

    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryLogArchiveRepository inventoryLogArchiveRepository;

    @Transactional
    public int archiveBatch(LocalDateTime cutoff, int batchSize) {
        List<InventoryLog> targets = inventoryLogRepository.findArchivableLogs(cutoff, batchSize);
        if (targets.isEmpty()) {
            return 0;
        }

        List<InventoryLogArchive> archives = targets.stream()
                .map(this::toArchive)
                .toList();

        inventoryLogArchiveRepository.saveAll(archives);
        inventoryLogRepository.deleteAllInBatch(targets);

        return archives.size();
    }

    private InventoryLogArchive toArchive(InventoryLog source) {
        return InventoryLogArchive.builder()
                .productId(source.getProductId())
                .productName(source.getProductName())
                .boxCode(source.getBoxCode())
                .transactionCode(source.getTransactionCode())
                .logType(source.getLogType())
                .quantity(source.getQuantity())
                .fromLocationType(source.getFromLocationType())
                .fromLocationId(source.getFromLocationId())
                .toLocationType(source.getToLocationType())
                .toLocationId(source.getToLocationId())
                .actorType(source.getActorType())
                .actorId(source.getActorId())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .deletedAt(source.getDeletedAt())
                .build();
    }
}
