package com.chaing.domain.inventorylogs.repository;

import com.chaing.domain.inventorylogs.entity.InventoryLog;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventorylogs.enums.ActorType;
import com.chaing.domain.inventorylogs.repository.interfaces.InventoryLogRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long>, InventoryLogRepositoryCustom {
    boolean existsByTransactionCodeAndBoxCodeAndLogTypeAndActorTypeAndActorIdAndDeletedAtIsNull(
            String transactionCode,
            String boxCode,
            LogType logType,
            ActorType actorType,
            Long actorId);
}
