package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementAdjustment;
import com.chaing.domain.settlements.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementAdjustmentRepository extends JpaRepository<SettlementAdjustment, Long> {

    @Query("SELECT s FROM SettlementAdjustment s " +
            "WHERE (:franchiseId IS NULL OR s.franchiseId = :franchiseId) " +
            "AND (:voucherType IS NULL OR s.voucherType = :voucherType) " +
            "AND (:startDate IS NULL OR s.occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR s.occurredAt <= :endDate)")
    Page<SettlementAdjustment> findByConditions(
            @Param("franchiseId") Long franchiseId,
            @Param("voucherType") VoucherType voucherType,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable);
}

