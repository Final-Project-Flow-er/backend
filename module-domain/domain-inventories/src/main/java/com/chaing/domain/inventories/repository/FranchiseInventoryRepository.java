package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.FranchiseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long> {
    List<FranchiseInventory> findAllBySerialCodeIn(List<String> serialCodes);

    List<FranchiseInventory> findAllByStatusInboundWait();

    List<FranchiseInventory> findAllByIdIn(List<Long> selectedList);

    @Modifying
    @Query("UPDATE FranchiseInventory i SET i.status = 'INBOUND' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(List<String> confirmedIds);
}
