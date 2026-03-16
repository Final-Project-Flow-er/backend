package com.chaing.api.facade.inventorylogs.scheduler;

import com.chaing.domain.inventorylogs.service.InventoryLogArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryLogArchiveScheduler {

    private final InventoryLogArchiveService inventoryLogArchiveService;

    @Value("${app.inventory-log.archive.retention-months:6}")
    private int retentionMonths;

    @Value("${app.inventory-log.archive.batch-size:2000}")
    private int batchSize;

    @Value("${app.inventory-log.archive.max-batches:50}")
    private int maxBatches;

    @Scheduled(cron = "${app.inventory-log.archive.cron:0 30 2 * * *}")
    public void archiveOldInventoryLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(retentionMonths);

        int movedTotal = 0;
        for (int i = 0; i < maxBatches; i++) {
            int moved = inventoryLogArchiveService.archiveBatch(cutoff, batchSize);
            movedTotal += moved;
            if (moved < batchSize) {
                break;
            }
        }

        if (movedTotal > 0) {
            log.info("Archived {} inventory logs older than {} months", movedTotal, retentionMonths);
        }
    }
}
