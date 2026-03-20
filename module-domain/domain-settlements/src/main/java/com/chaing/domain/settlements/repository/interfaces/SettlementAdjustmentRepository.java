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
            "AND (:settlementMonth IS NULL OR s.settlementMonth = :settlementMonth)")
    Page<SettlementAdjustment> findByConditions(
            @Param("franchiseId") Long franchiseId,
            @Param("voucherType") VoucherType voucherType,
            @Param("settlementMonth") String settlementMonth,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(CASE WHEN s.isMinus = true THEN -s.adjustmentAmount ELSE s.adjustmentAmount END), 0) " +
            "FROM SettlementAdjustment s " +
            "WHERE s.settlementMonth = :settlementMonth " +
            "AND (:franchiseId IS NULL OR s.franchiseId = :franchiseId)")
    java.math.BigDecimal sumAdjustmentAmountByMonth(
            @Param("settlementMonth") String settlementMonth,
            @Param("franchiseId") Long franchiseId);
}

