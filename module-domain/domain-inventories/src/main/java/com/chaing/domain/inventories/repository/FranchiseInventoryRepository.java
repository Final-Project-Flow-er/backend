package com.chaing.domain.inventories.repository;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.interfaces.FranchiseInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long>, FranchiseInventoryRepositoryCustom {
    List<FranchiseInventory> findAllBySerialCodeIn(List<String> serialCodes);

    List<FranchiseInventory> findAllByOrderId(Long orderId);

    List<FranchiseInventory> findAllByOrderItemIdIn(List<Long> orderItemIds);

    List<FranchiseInventory> findAllByBoxCodeIn(List<String> boxCodes);

    List<FranchiseInventory> findAllByStatus(LogType status);

    @Modifying
    @Query("UPDATE FranchiseInventory i SET i.status = 'com.chaing.core.enums.LogType.AVAILABLE' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(@Param("serials") List<String> serials);

    @Query("SELECT DISTINCT fi.franchiseId FROM FranchiseInventory fi")
    List<Long> getAllFranchiseIds();

    void deleteByFranchiseIdAndInventoryIdIn(Long aLong, List<Long> longs);

    List<FranchiseInventory> findByInventoryIdIn(List<Long> ids);
    List<FranchiseInventory> findByInventoryIdInAndFranchiseId(List<Long> ids, Long franchiseId);
    List<FranchiseInventory> findByBoxCodeInAndFranchiseId(List<String> boxCodes, Long franchiseId);
}
