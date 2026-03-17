package com.chaing.domain.inventorylogs.repository;

import com.chaing.domain.inventorylogs.entity.InventoryLogArchive;
import com.chaing.domain.inventorylogs.repository.interfaces.InventoryLogArchiveRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogArchiveRepository
        extends JpaRepository<InventoryLogArchive, Long>, InventoryLogArchiveRepositoryCustom {
}
