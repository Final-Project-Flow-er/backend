package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.interfaces.FranchiseInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long>, FranchiseInventoryRepositoryCustom {
    List<FranchiseInventory> findAllBySerialCodeIn(List<String> serialCodes);

    List<FranchiseInventory> findAllByOrderItemIdIn(List<Long> orderItemIds);

    List<FranchiseInventory> findAllByBoxCodeIn(List<String> boxCodes);

    @Query("SELECT fi.franchiseId FROM FranchiseInventory fi")
    List<Long> getAllFranchiseIds();
}
