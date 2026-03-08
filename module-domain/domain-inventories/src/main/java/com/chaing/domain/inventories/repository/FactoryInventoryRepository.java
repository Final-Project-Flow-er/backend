package com.chaing.domain.inventories.repository;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import com.chaing.domain.inventories.repository.interfaces.FactoryInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryInventoryRepository extends JpaRepository<FactoryInventory,Long>, FactoryInventoryRepositoryCustom {

    List<FactoryInventory> findAllByStatus(LogType status);

    @Modifying
    @Query("UPDATE FactoryInventory i SET i.status = 'INBOUND' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(@Param("serials") List<String> serials);

    List<FactoryInventory> findAllBySerialCodeIn(List<String> selectedList);

    @Modifying
    @Query("update FactoryInventory f set f.status = :targetStatus where f.serialCode in :ids")
    void setTargetStatusBySerialCodeIn(
            @Param("ids") List<String> confirmedIds,
            @Param("targetStatus") LogType targetStatus);

    @Modifying
    @Query("update FactoryInventory f set f.boxCode = :boxCode where f.serialCode in :ids")
    void setBoxCode(
            @Param("boxCode") String boxCode,
            @Param("ids") List<String> selectedList);

    @Modifying(clearAutomatically = true)
    @Query("update FactoryInventory f " +
            "set f.boxCode = null, " +
            "    f.status = com.chaing.core.enums.LogType.AVAILABLE " + // Enum 경로를 맞춰주세요!
            "where f.serialCode in :ids")
    void cancelOutboundBySerialCodeIn(@Param("ids") List<String> confirmedIds);
}
