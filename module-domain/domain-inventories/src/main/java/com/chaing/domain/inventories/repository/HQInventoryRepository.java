package com.chaing.domain.inventories.repository;

import com.chaing.domain.inventories.entity.HQInventory;
import com.chaing.domain.inventories.repository.interfaces.HQInventoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HQInventoryRepository extends JpaRepository<HQInventory, Long>, HQInventoryRepositoryCustom {
    void deleteByInventoryIdIn(List<Long> longs);
}
