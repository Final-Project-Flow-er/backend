package com.chaing.domain.inventories.repository;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.FactoryInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryInventoryRepository extends JpaRepository<FactoryInventory,Long> {

    List<FactoryInventory> findAllByStatus(LogType status);

    @Modifying
    @Query("UPDATE FactoryInventory i SET i.status = 'INBOUND' WHERE i.serialCode IN :serials")
    void updateAllStatusInboundBySerialCode(@Param("serials") List<String> serials);

    List<FactoryInventory> findAllByInventoryIdIn(List<Long> selectedList);
}
