package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.repository.interfaces.FactoryInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoryInventoryRepository extends JpaRepository<FactoryInventory,Long>, FactoryInventoryRepositoryCustom {
}
