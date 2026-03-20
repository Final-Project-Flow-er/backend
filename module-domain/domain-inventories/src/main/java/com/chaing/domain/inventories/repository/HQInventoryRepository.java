package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.HQInventory;
import com.chaing.domain.inventories.repository.interfaces.HQInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HQInventoryRepository extends JpaRepository<HQInventory, Long>, HQInventoryRepositoryCustom {
    List<HQInventory> findAllByBoxCodeInAndDeletedAtIsNull(List<String> requestedBoxCodes);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from HQInventory h where h.inventoryId in :ids")
    void deleteByInventoryIdIn(@Param("ids") List<Long> ids);
    List<HQInventory> findByInventoryIdIn(List<Long> ids);
    List<HQInventory> findByBoxCodeIn(List<String> boxCodes);

    List<HQInventory> findAllByOrderItemIdInAndDeletedAtIsNull(List<Long> orderItemIds);

    List<HQInventory> findAllBySerialCodeInAndDeletedAtIsNull(List<String> serialCodes);
}
