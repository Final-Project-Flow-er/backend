package com.chaing.domain.inventories.repository.impl;

import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.entity.QHQInventory;
import com.chaing.domain.inventories.entity.QInventoryPolicy;
import com.chaing.domain.inventories.repository.interfaces.HQInventoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HQInventoryRepositoryImpl implements HQInventoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QHQInventory hqInventory = QHQInventory.hQInventory;
    private final QInventoryPolicy inventoryPolicy = QInventoryPolicy.inventoryPolicy;
    private final EntityManager em;

    @Override
    public void deleteHQInventory(List<String> serialCode) {
        queryFactory
                .delete(hqInventory)
                .where(hqInventory.serialCode.in(serialCode))
                .execute();
        em.flush();
        em.clear();
    }

    @Override
    public long updateExpiredStatus(LocalDate expirationDate) {
        long updatedCount = queryFactory
                .update(hqInventory)
                .set(hqInventory.status, LogType.EXPIRED)
                .where(
                        hqInventory.manufactureDate.loe(expirationDate),
                        hqInventory.status.eq(LogType.AVAILABLE))
                .execute();

        em.flush();
        em.clear();

        return updatedCount;
    }

}
