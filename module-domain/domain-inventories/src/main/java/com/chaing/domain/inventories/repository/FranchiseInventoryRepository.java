package com.chaing.domain.inventories.repository;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long> {
    List<FranchiseInventory> findAllBySerialCodeIn(List<String> serialCodes);

    List<FranchiseInventory> findAllByOrderItemIdIn(List<Long> orderItemIds);

    List<FranchiseInventory> findAllByBoxCodeIn(List<String> boxCodes);

    List<FranchiseInventory> findAllByStatus(LogType status);

    List<FranchiseInventory> findAllByInventoryIdIn(List<Long> selectedList);

    @Modifying
    @Query("UPDATE FranchiseInventory i SET i.status = 'INBOUND' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(@Param("serials") List<String> serials);
}
