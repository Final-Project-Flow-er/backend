package com.chaing.domain.inventories.repository;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FranchiseInventory;
import com.chaing.domain.inventories.repository.interfaces.FranchiseInventoryRepositoryCustom;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseInventoryRepository extends JpaRepository<FranchiseInventory, Long>, FranchiseInventoryRepositoryCustom {
    List<FranchiseInventory> findAllBySerialCodeInAndDeletedAtIsNull(List<String> serialCodes);

    List<FranchiseInventory> findAllByOrderId(Long orderId);

    List<FranchiseInventory> findAllByOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<FranchiseInventory> findAllByBoxCodeIn(List<String> boxCodes);

    List<FranchiseInventory> findAllByBoxCodeInAndDeletedAtIsNull(List<String> boxCodes);

    List<FranchiseInventory> findAllByStatus(LogType status);

    @Modifying
    @Query("UPDATE FranchiseInventory i SET i.status = 'AVAILABLE' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(@Param("serials") List<String> serials);

    @Query("SELECT DISTINCT fi.franchiseId FROM FranchiseInventory fi")
    List<Long> getAllFranchiseIds();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FranchiseInventory f where f.franchiseId = :franchiseId and f.inventoryId in :ids")
    void deleteByFranchiseIdAndInventoryIdIn(@Param("franchiseId") Long franchiseId, @Param("ids") List<Long> ids);

    List<FranchiseInventory> findAllByBoxCode(String boxCode);
    List<FranchiseInventory> findByInventoryIdIn(List<Long> ids);
    List<FranchiseInventory> findByInventoryIdInAndFranchiseId(List<Long> ids, Long franchiseId);
    List<FranchiseInventory> findByBoxCodeInAndFranchiseId(List<String> boxCodes, Long franchiseId);

    @Query("select fi from FranchiseInventory fi where fi.status = :status and fi.serialCode in (:serialCodes) and fi.franchiseId = :franchiseId")
    List<FranchiseInventory> getAllByStatusAndSerialCodeAndFranchiseId(
            @Param("serialCodes") @NotEmpty(message = "선택된 제품이 존재하지 않습니다.") List<String> serialCodes,
            @Param("franchiseId") Long franchiseId,
            @Param("status") LogType status);

    List<FranchiseInventory> getAllByFranchiseIdAndSerialCodeIn(
            @Param("franchiseId") Long franchiseId,
            @Param("serialCodes") List<String> serialCodes);
}
