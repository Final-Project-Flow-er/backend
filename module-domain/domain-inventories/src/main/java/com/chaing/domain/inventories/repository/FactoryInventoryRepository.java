package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.FactoryInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryInventoryRepository extends JpaRepository<FactoryInventory,Integer> {

    List<FactoryInventory> findAllByStatusInboundWait();

}
