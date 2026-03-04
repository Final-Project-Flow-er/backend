package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.FranchiseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long> {
    List<FranchiseInventory> findAllBySerialCodeIn(List<String> serialCodes);
}
